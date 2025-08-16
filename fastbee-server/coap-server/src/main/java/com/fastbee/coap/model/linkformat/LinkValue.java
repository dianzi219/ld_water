package com.fastbee.coap.model.linkformat;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Slf4j
public class LinkValue {

    //*******************************************************************************************
    // static fields and methods
    //*******************************************************************************************

    static LinkValue decode(String linkValue) {
        LinkValue result = new LinkValue(getUriReference(linkValue));
        for (String linkParam : LinkValue.getLinkParams(linkValue)) {
            result.addLinkParam(LinkParam.decode(linkParam));
        }
        return result;
    }

    private static String getUriReference(String linkValue) {
        String uriReference = linkValue.substring(linkValue.indexOf("<") + 1, linkValue.indexOf(">"));
        log.info("Found URI reference <{}>", uriReference);
        return uriReference;
    }

    private static List<String> getLinkParams(String linkValue) {
        String[] linkParams = linkValue.split(";");
        return new ArrayList<>(Arrays.asList(linkParams).subList(1, linkParams.length));
    }


    //******************************************************************************************
    // instance related fields and methods
    //******************************************************************************************

    private final String uriReference;
    private final Collection<LinkParam> linkParams;

    /**
     * Creates a new instance of {@link LinkValue}
     * @param uriReference the URI reference, i.e. the resource to be described
     * @param linkParams the {@link LinkParam}s to describe the resource
     */
    public LinkValue(String uriReference, Collection<LinkParam> linkParams) {
        this.uriReference = uriReference;
        this.linkParams = linkParams;
    }

    private LinkValue(String uriReference) {
        this(uriReference, new ArrayList<LinkParam>());
    }

    private void addLinkParam(LinkParam linkParams) {
        this.linkParams.add(linkParams);
    }

    /**
     * Returns the URI reference of this {@link LinkValue}
     * @return the URI reference of this {@link LinkValue}
     */
    public String getUriReference() {
        return this.uriReference;
    }

    /**
     * Returns the {@link LinkParam}s describing the resource identified by the URI reference
     * @return the {@link LinkParam}s describing this resource identified by the URI reference
     */
    public Collection<LinkParam> getLinkParams() {
        return this.linkParams;
    }

    /**
     * Returns <code>true</code> if this {@link LinkValue} contains a {@link LinkParam} that matches the given
     * criterion, i.e. the given key-value-pair and <code>false</code> otherwise.
     *
     * @param key the key of the criterion
     * @param value the value of the criterion
     *
     * @return <code>true</code> if this {@link LinkValue} contains a {@link LinkParam} that matches the given
     * criterion, i.e. the given key-value-pair and <code>false</code> otherwise.
     */
    public boolean containsLinkParam(LinkParam.Key key, String value) {
        for (LinkParam linkParam : this.linkParams) {
            if (key.equals(linkParam.getKey())) {
                return value == null || linkParam.contains(value);
            }
        }
        return false;
    }

    /**
     * Returns a string representation of this {@link LinkValue}.
     * @return a string representation of this {@link LinkValue}.
     */
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("<").append(uriReference).append(">");
        for (LinkParam linkParam : this.getLinkParams()) {
            builder.append(";").append(linkParam.toString());
        }
        return builder.toString();
    }
}
