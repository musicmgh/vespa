# Copyright 2017 Yahoo Holdings. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
vespa_add_library(searchlib_queryeval OBJECT
    SOURCES
    andnotsearch.cpp
    andsearch.cpp
    blueprint.cpp
    booleanmatchiteratorwrapper.cpp
    create_blueprint_visitor_helper.cpp
    document_weight_search_iterator.cpp
    dot_product_blueprint.cpp
    dot_product_search.cpp
    emptysearch.cpp
    equiv_blueprint.cpp
    equivsearch.cpp
    fake_requestcontext.cpp
    fake_result.cpp
    fake_search.cpp
    fake_searchable.cpp
    field_spec.cpp
    get_weight_from_node.cpp
    hitcollector.cpp
    intermediate_blueprints.cpp
    isourceselector.cpp
    iterator_pack.cpp
    iterators.cpp
    leaf_blueprints.cpp
    monitoring_dump_iterator.cpp
    monitoring_search_iterator.cpp
    multibitvectoriterator.cpp
    multisearch.cpp
    nearsearch.cpp
    orsearch.cpp
    predicate_blueprint.cpp
    predicate_search.cpp
    ranksearch.cpp
    same_element_blueprint.cpp
    same_element_search.cpp
    searchable.cpp
    searchiterator.cpp
    simple_phrase_blueprint.cpp
    simple_phrase_search.cpp
    simpleresult.cpp
    simplesearch.cpp
    sourceblendersearch.cpp
    split_float.cpp
    termasstring.cpp
    termwise_blueprint_helper.cpp
    termwise_search.cpp
    truesearch.cpp
    unpackinfo.cpp
    weighted_set_term_blueprint.cpp
    weighted_set_term_search.cpp
    DEPENDS
)
