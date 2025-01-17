package ca.nengo.sim.model.impl;

import ca.nengo.sim.model.Node;

/**
* Network implementation that stores its index as a LinkedHashMap
*/
public class DefaultNetwork<N extends Node> extends AbstractMapNetwork<String,N> {


    public DefaultNetwork() {
        super();
    }

    public DefaultNetwork(String s) {
        super(s);
    }

    @Override
    public String name(N node) {
        return node.name();
    }

}
