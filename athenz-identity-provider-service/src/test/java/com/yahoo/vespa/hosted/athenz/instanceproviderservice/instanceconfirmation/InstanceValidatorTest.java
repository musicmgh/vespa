// Copyright 2018 Yahoo Holdings. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.vespa.hosted.athenz.instanceproviderservice.instanceconfirmation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.yahoo.config.model.api.ApplicationInfo;
import com.yahoo.config.model.api.HostInfo;
import com.yahoo.config.model.api.Model;
import com.yahoo.config.model.api.ServiceInfo;
import com.yahoo.config.model.api.SuperModel;
import com.yahoo.config.model.api.SuperModelProvider;
import com.yahoo.config.provision.ApplicationId;
import com.yahoo.vespa.athenz.identityprovider.api.EntityBindingsMapper;
import com.yahoo.vespa.athenz.identityprovider.api.bindings.IdentityDocumentEntity;
import com.yahoo.vespa.athenz.identityprovider.api.bindings.SignedIdentityDocumentEntity;
import com.yahoo.vespa.athenz.identityprovider.api.bindings.VespaUniqueInstanceIdEntity;
import com.yahoo.vespa.hosted.athenz.instanceproviderservice.impl.Utils;
import org.junit.Test;

import java.net.URI;
import java.security.PrivateKey;
import java.security.Signature;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.yahoo.vespa.hosted.athenz.instanceproviderservice.instanceconfirmation.InstanceValidator.SERVICE_PROPERTIES_DOMAIN_KEY;
import static com.yahoo.vespa.hosted.athenz.instanceproviderservice.instanceconfirmation.InstanceValidator.SERVICE_PROPERTIES_SERVICE_KEY;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author valerijf
 */
public class InstanceValidatorTest {

    private final ApplicationId applicationId = ApplicationId.from("tenant", "application", "instance");
    private final String domain = "domain";
    private final String service = "service";


    @Test
    public void application_does_not_exist() {
        SuperModelProvider superModelProvider = mockSuperModelProvider();
        InstanceValidator instanceValidator = new InstanceValidator(null, superModelProvider);

        assertFalse(instanceValidator.isSameIdentityAsInServicesXml(applicationId, domain, service));
    }

    @Test
    public void application_does_not_have_domain_set() {
        SuperModelProvider superModelProvider = mockSuperModelProvider(
                mockApplicationInfo(applicationId, 5, Collections.emptyList()));
        InstanceValidator instanceValidator = new InstanceValidator(null, superModelProvider);

        assertFalse(instanceValidator.isSameIdentityAsInServicesXml(applicationId, domain, service));
    }

    @Test
    public void application_has_wrong_domain() {
        ServiceInfo serviceInfo = new ServiceInfo("serviceName", "type", Collections.emptyList(),
                Collections.singletonMap(SERVICE_PROPERTIES_DOMAIN_KEY, "not-domain"), "confId", "hostName");

        SuperModelProvider superModelProvider = mockSuperModelProvider(
                mockApplicationInfo(applicationId, 5, Collections.singletonList(serviceInfo)));
        InstanceValidator instanceValidator = new InstanceValidator(null, superModelProvider);

        assertFalse(instanceValidator.isSameIdentityAsInServicesXml(applicationId, domain, service));
    }

    @Test
    public void application_has_same_domain_and_service() {
        Map<String, String> properties = new HashMap<>();
        properties.put(SERVICE_PROPERTIES_DOMAIN_KEY, domain);
        properties.put(SERVICE_PROPERTIES_SERVICE_KEY, service);

        ServiceInfo serviceInfo = new ServiceInfo("serviceName", "type", Collections.emptyList(),
                properties, "confId", "hostName");

        SuperModelProvider superModelProvider = mockSuperModelProvider(
                mockApplicationInfo(applicationId, 5, Collections.singletonList(serviceInfo)));
        InstanceValidator instanceValidator = new InstanceValidator(null, superModelProvider);

        assertTrue(instanceValidator.isSameIdentityAsInServicesXml(applicationId, domain, service));
    }

    private static InstanceConfirmation createInstanceConfirmation(PrivateKey privateKey, ApplicationId applicationId,
                                                                   String domain, String service) {
        IdentityDocumentEntity identityDocument = new IdentityDocumentEntity(
                new VespaUniqueInstanceIdEntity(applicationId.tenant().value(), applicationId.application().value(),
                                                "environment", "region", applicationId.instance().value(), "cluster-id", 0),
                "hostname",
                "instance-hostname",
                Instant.now(),
                ImmutableSet.of("127.0.0.1", "::1"));

        try {
            ObjectMapper mapper = Utils.getMapper();
            String encodedIdentityDocument =
                    Base64.getEncoder().encodeToString(mapper.writeValueAsString(identityDocument).getBytes());
            Signature sigGenerator = Signature.getInstance("SHA512withRSA");
            sigGenerator.initSign(privateKey);
            sigGenerator.update(encodedIdentityDocument.getBytes());

            return new InstanceConfirmation(
                    "provider", domain, service,
                    new SignedIdentityDocumentEntity(encodedIdentityDocument,
                                                     Base64.getEncoder().encodeToString(sigGenerator.sign()),
                                                     0,
                                                     EntityBindingsMapper.toVespaUniqueInstanceId(identityDocument.providerUniqueId).asDottedString(),
                                                     "dnssuffix",
                                                     "service",
                                                     URI.create("http://localhost/zts"),
                                                     1,
                                                     identityDocument.configServerHostname,
                                                     identityDocument.instanceHostname,
                                                     identityDocument.createdAt,
                                                     identityDocument.ipAddresses,
                                                     null)); // TODO Remove support for legacy representation without type
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private SuperModelProvider mockSuperModelProvider(ApplicationInfo... appInfos) {
        SuperModel superModel = new SuperModel(Stream.of(appInfos)
                .collect(Collectors.groupingBy(
                        appInfo -> appInfo.getApplicationId().tenant(),
                        Collectors.toMap(
                                ApplicationInfo::getApplicationId,
                                Function.identity()
                        )
                )));

        SuperModelProvider superModelProvider = mock(SuperModelProvider.class);
        when(superModelProvider.getSuperModel()).thenReturn(superModel);
        return superModelProvider;
    }

    private ApplicationInfo mockApplicationInfo(ApplicationId appId, int numHosts, List<ServiceInfo> serviceInfo) {
        List<HostInfo> hosts = IntStream.range(0, numHosts)
                .mapToObj(i -> new HostInfo("host-" + i + "." + appId.toShortString() + ".yahoo.com", serviceInfo))
                .collect(Collectors.toList());

        Model model = mock(Model.class);
        when(model.getHosts()).thenReturn(hosts);

        return new ApplicationInfo(appId, 0, model);
    }
}
