package org.dsa.iot.dslink.node;

import org.dsa.iot.dslink.node.exceptions.NoSuchPathException;
import org.dsa.iot.dslink.util.NodePair;
import org.dsa.iot.dslink.util.StringUtils;

import java.util.Map;

/**
 * Handles nodes based on paths.
 *
 * @author Samuel Grenier
 */
public class NodeManager {

    // Fake root to provide a listing on "/"
    private final Node superRoot;
    private final String defaultProfile;

    public NodeManager(SubscriptionManager manager, String defaultProfile) {
        this.superRoot = new Node(null, defaultProfile, null, manager);
        this.defaultProfile = defaultProfile;
    }

    public Node createRootNode(String name, String profile) {
        return superRoot.createChild(name, profile);
    }

    public Map<String, Node> getChildren(String path) {
        Node child = getNode(path).getNode();
        if (child == null)
            throw new NoSuchPathException(path);
        return child.getChildren();
    }

    public NodePair getNode(String path) {
        return getNode(path, false);
    }

    public NodePair getNode(String path, boolean create) {
        if (path == null)
            throw new NullPointerException("path");
        else if ("/".equals(path))
            return new NodePair(superRoot, null);
        String[] parts = splitPath(path);
        if (parts.length == 1 && StringUtils.isReference(parts[0])) {
            return new NodePair(superRoot, parts[0]);
        }
        Node current = superRoot.getChild(parts[0]);
        if (create && current == null) {
            current = superRoot.createChild(Node.checkName(parts[0]), defaultProfile);
        }
        for (int i = 1; i < parts.length; i++) {
            if (current == null) {
                break;
            } else if (i + 1 == parts.length && StringUtils.isReference(parts[i])) {
                return new NodePair(current, parts[i]);
            } else {
                Node temp = current.getChild(parts[i]);
                if (create && temp == null) {
                    temp = current.createChild(Node.checkName(parts[i]), defaultProfile);
                }
                current = temp;
            }
        }
        if (current == null)
            throw new NoSuchPathException(path);
        return new NodePair(current, null);
    }

    public static String[] splitPath(String path) {
        return normalizePath(path).split("/");
    }

    public static String normalizePath(String path) {
        return normalizePath(path, false);
    }

    public static String normalizePath(String path, boolean leading) {
        if (path == null || path.isEmpty())
            throw new IllegalArgumentException("path null or empty");
        else if (path.contains("//"))
            throw new IllegalArgumentException("path contains //");
        else if ("/".equals(path))
            return path;

        // Examine leading character
        if (!leading && path.startsWith("/"))
            path = path.substring(1);
        else if (leading && !path.startsWith("/"))
            path = "/" + path;

        // Remove ending character
        if (path.endsWith("/"))
            path = path.substring(0, path.length() - 1);
        return path;
    }
}
