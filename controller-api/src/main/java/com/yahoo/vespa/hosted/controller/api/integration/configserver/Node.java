// Copyright 2018 Yahoo Holdings. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.vespa.hosted.controller.api.integration.configserver;

import com.yahoo.component.Version;
import com.yahoo.config.provision.ApplicationId;
import com.yahoo.config.provision.HostName;
import com.yahoo.config.provision.NodeType;
import org.jetbrains.annotations.TestOnly;

import java.util.Objects;
import java.util.Optional;

/**
 * A node in hosted Vespa.
 *
 * @author mpolden
 * @author jonmv
 */
public class Node {

    private final HostName hostname;
    private final State state;
    private final NodeType type;
    private final Optional<ApplicationId> owner;
    private final Version currentVersion;
    private final Version wantedVersion;
    private final ServiceState serviceState;
    private final long restartGeneration;
    private final long wantedRestartGeneration;
    private final long rebootGeneration;
    private final long wantedRebootGeneration;

    public Node(HostName hostname, State state, NodeType type, Optional<ApplicationId> owner,
                Version currentVersion, Version wantedVersion, ServiceState serviceState,
                long restartGeneration, long wantedRestartGeneration, long rebootGeneration, long wantedRebootGeneration) {
        this.hostname = hostname;
        this.state = state;
        this.type = type;
        this.owner = owner;
        this.currentVersion = currentVersion;
        this.wantedVersion = wantedVersion;
        this.serviceState = serviceState;
        this.restartGeneration = restartGeneration;
        this.wantedRestartGeneration = wantedRestartGeneration;
        this.rebootGeneration = rebootGeneration;
        this.wantedRebootGeneration = wantedRebootGeneration;
    }

    @TestOnly
    public Node(HostName hostname, State state, NodeType type, Optional<ApplicationId> owner,
                Version currentVersion, Version wantedVersion) {
        this(hostname, state, type, owner, currentVersion, wantedVersion,
             ServiceState.unorchestrated, 0, 0, 0, 0);
    }

    public HostName hostname() {
        return hostname;
    }

    public State state() { return state; }

    public NodeType type() {
        return type;
    }

    public Optional<ApplicationId> owner() {
        return owner;
    }

    public Version currentVersion() {
        return currentVersion;
    }

    public Version wantedVersion() {
        return wantedVersion;
    }

    public ServiceState serviceState() {
        return serviceState;
    }

    public long restartGeneration() {
        return restartGeneration;
    }

    public long wantedRestartGeneration() {
        return wantedRestartGeneration;
    }

    public long rebootGeneration() {
        return rebootGeneration;
    }

    public long wantedRebootGeneration() {
        return wantedRebootGeneration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(hostname, node.hostname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostname);
    }

    /** Known node states */
    public enum State {
        provisioned,
        ready,
        reserved,
        active,
        inactive,
        dirty,
        failed,
        parked
    }

    /** Known node states with regards to service orchestration */
    public enum ServiceState {
        expectedUp,
        allowedDown,
        unorchestrated
    }

}
