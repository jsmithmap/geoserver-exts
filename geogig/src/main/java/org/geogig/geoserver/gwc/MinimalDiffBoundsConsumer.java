/* Copyright (c) 2014 OpenPlans. All rights reserved.
 * This code is licensed under the GNU GPL 2.0 license, available at the root
 * application directory.
 */
package org.geogig.geoserver.gwc;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import org.geotools.geometry.jts.JTS;
import org.locationtech.geogig.api.Bounded;
import org.locationtech.geogig.api.Bucket;
import org.locationtech.geogig.api.Node;
import org.locationtech.geogig.api.NodeRef;
import org.locationtech.geogig.api.plumbing.diff.DiffTreeVisitor;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

class MinimalDiffBoundsConsumer implements DiffTreeVisitor.Consumer {

    private static final GeometryFactory GEOM_FACTORY = CompactMultiPoint.GEOM_FACTORY;

    /**
     * Accumulates punctual differences to save heap
     */
    private CompactMultiPoint points = new CompactMultiPoint();

    /**
     * Accumulates non punctual differences (i.e. bounding polygons)
     */
    private List<Geometry> nonPoints = new LinkedList<Geometry>();

    private Predicate<Node> treeNodeFilter = Predicates.alwaysTrue();

    private Envelope envBuff = new Envelope();

    public void setTreeNameFilter(final String treeName) {
        checkArgument(!isNullOrEmpty(treeName), "treeName can't be null or empty");
        this.treeNodeFilter = new Predicate<Node>() {
            private final String name = treeName;

            /**
             * @return true if node name is null, empty, or equal to {@code treeName}
             */
            @Override
            public boolean apply(Node node) {
                return node == null || NodeRef.ROOT.equals(node.getName())
                        || name.equals(node.getName());
            }
        };
    }

    /**
     * @return a single geometry product of unioning all the bounding boxes acquired while
     *         traversing the diff
     */
    public Geometry buildGeometry() {
        List<Geometry> geomList = nonPoints;
        nonPoints = null;
        if (!points.isEmpty()) {
            geomList.add(points);
        }
        points = null;

        Geometry buildGeometry = GEOM_FACTORY.buildGeometry(geomList);
        geomList.clear();
        Geometry union = buildGeometry.union();
        return union;
    }

    @Override
    public void feature(@Nullable Node left, @Nullable Node right) {
        addEnv(left);
        addEnv(right);
    }

    @Override
    public boolean tree(@Nullable Node left, @Nullable Node right) {
        if (!treeNodeFilter.apply(left) || !treeNodeFilter.apply(right)) {
            return false;
        }
        if (left == null) {
            addEnv(right);
            return false;
        } else if (right == null) {
            addEnv(left);
            return false;
        }
        return true;
    }

    @Override
    public boolean bucket(int bucketIndex, int bucketDepth, Bucket left, Bucket right) {
        if (left == null) {
            addEnv(right);
            return false;
        } else if (right == null) {
            addEnv(left);
            return false;
        }
        return true;
    }

    private void addEnv(@Nullable Bounded node) {
        if (node == null) {
            return;
        }
        Envelope env = envBuff;
        env.setToNull();
        node.expand(env);
        if (env.isNull()) {
            return;
        }
        if (isPoint(env)) {
            points.add(env.getMinX(), env.getMinY());
        } else if (isOrthoLine(env)) {
            // handle the case where the envelope is given by an orthogonal line so we don't add a
            // zero area polygon
            double width = env.getWidth();
            GrowableCoordinateSequence cs = new GrowableCoordinateSequence();
            if (width == 0D) {
                cs.add(env.getMinX(), env.getMinY());
                cs.add(env.getMinX(), env.getMaxY());
            } else {
                cs.add(env.getMinX(), env.getMinY());
                cs.add(env.getMaxX(), env.getMinY());
            }
            nonPoints.add(GEOM_FACTORY.createLineString(cs));
        } else {
            nonPoints.add(JTS.toGeometry(env, GEOM_FACTORY));
        }
    }

    private boolean isOrthoLine(Envelope env) {
        return env.getArea() == 0D && env.getWidth() > 0D || env.getHeight() > 0D;
    }

    private boolean isPoint(Envelope env) {
        return env.getWidth() == 0D && env.getHeight() == 0D;
    }

    @Override
    public void endTree(Node left, Node right) {
        // nothing to do, intentionally blank
    }

    @Override
    public void endBucket(int bucketIndex, int bucketDepth, Bucket left, Bucket right) {
        // nothing to do, intentionally blank
    }
}
