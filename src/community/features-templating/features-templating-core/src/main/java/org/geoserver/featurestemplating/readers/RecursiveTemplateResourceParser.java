package org.geoserver.featurestemplating.readers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geoserver.platform.FileWatcher;
import org.geoserver.platform.resource.Resource;

public class RecursiveTemplateResourceParser {

    protected RecursiveTemplateResourceParser parent;

    protected Resource resource;

    private List<FileWatcher<Object>>  watchers;

    protected static final int MAX_RECURSION_DEPTH =
            Integer.parseInt(System.getProperty("GEOSERVER_FT_MAX_DEPTH", "50"));

    public RecursiveTemplateResourceParser(
            Resource resource, RecursiveTemplateResourceParser parent) {
        this.resource = resource;
        validateResource(resource);
        this.parent = parent;
        this.watchers=parent.getWatchers();
        validateDepth();
        addFileWatcher(resource);
    }

    public RecursiveTemplateResourceParser(Resource resource) {
        this.resource = resource;
        validateResource(resource);
        this.parent = null;
        addFileWatcher(resource);
    }

    protected void validateResource(Resource resource) {
        if (!resource.getType().equals(Resource.Type.RESOURCE))
            throw new IllegalArgumentException("Path " + resource.path() + " does not exist");
    }

    /**
     * Returns the list of inclusions, starting from the top-most parent and walking down to the
     * current reader
     */
    protected List<String> getInclusionChain() {
        List<String> resources = new ArrayList<>();
        RecursiveTemplateResourceParser curr = this;
        while (curr != null) {
            resources.add(curr.resource.path());
            curr = curr.parent;
        }
        Collections.reverse(resources);
        return resources;
    }

    protected int getDepth() {
        int depth = 0;
        RecursiveTemplateResourceParser curr = this.parent;
        while (curr != null) {
            curr = curr.parent;
            depth++;
        }
        return depth;
    }

    protected Resource getResource(Resource resource, String path) {
        // relative paths are
        if (path.startsWith("./")) path = path.substring(2);
        if (path.startsWith("/")) return getRoot(resource).get(path);
        return resource.parent().get(path);
    }

    /** API is not 100% clear, but going up should lead us to the root of the virtual file system */
    private Resource getRoot(Resource resource) {
        Resource r = resource;
        Resource parent = r.parent();
        while (parent != null && !parent.equals(r)) {
            r = parent;
            parent = r.parent();
        }

        return r;
    }

    protected void validateDepth() {
        int depth = getDepth();
        if (depth > MAX_RECURSION_DEPTH)
            throw new RuntimeException(
                    "Went beyond maximum nested inclusion depth ("
                            + depth
                            + "), inclusion chain is: "
                            + getInclusionChain());
    }

    private void addFileWatcher(Resource resource){
        if (this.watchers==null) this.watchers=new ArrayList<>();
        this.watchers.add(new FileWatcher<>(resource));
    }

    public List<FileWatcher<Object>> getWatchers(){
        return watchers;
    }
}
