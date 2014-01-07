/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright (c) 2013 Edugility LLC.
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT.  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * The original copy of this license is available at
 * http://www.opensource.org/license/mit-license.html.
 */
package com.edugility.maven;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;

import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.DefaultResolutionErrorHandler;
import org.apache.maven.artifact.resolver.ResolutionErrorHandler;

import org.apache.maven.artifact.resolver.filter.ArtifactFilter;

import org.apache.maven.artifact.repository.ArtifactRepository;

import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;

import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;

import org.apache.maven.shared.dependency.graph.traversal.CollectingDependencyNodeVisitor;

public class Artifacts {
  
  public Artifacts() {
    super();
  }
  
  public Collection<? extends Artifact> getDependencyArtifactsInTopologicalOrder(final MavenProject project,
                                                                                 final DependencyGraphBuilder dependencyGraphBuilder, 
                                                                                 final ArtifactFilter filter,
                                                                                 final ArtifactResolver resolver,
                                                                                 final ArtifactRepository localRepository)
    throws DependencyGraphBuilderException, ArtifactResolutionException {
    if (project == null) {
      throw new IllegalArgumentException("project", new NullPointerException("project"));
    }
    if (dependencyGraphBuilder == null) {
      throw new IllegalArgumentException("dependencyGraphBuilder", new NullPointerException("dependencyGraphBuilder"));
    }
    if (resolver == null) {
      throw new IllegalArgumentException("resolver", new NullPointerException("resolver"));
    }

    List<Artifact> returnValue = null;

    final Artifact projectArtifact = project.getArtifact();
    assert projectArtifact != null;
    assert projectArtifact.isResolved();

    final DependencyNode projectNode = dependencyGraphBuilder.buildDependencyGraph(project, filter);
    assert projectNode != null;

    final CollectingDependencyNodeVisitor visitor = new CollectingDependencyNodeVisitor();
    projectNode.accept(visitor);

    final Collection<? extends DependencyNode> nodes = visitor.getNodes();
    if (nodes != null && !nodes.isEmpty()) {

      returnValue = new ArrayList<Artifact>();

      for (final DependencyNode node : nodes) {
        if (node != null) {
          Artifact artifact = node.getArtifact();
          if (artifact != null) {

            if (!artifact.isResolved()) {
              // First see if the project's associated artifact map
              // contains a resolved version of this artifact.  The
              // artifact map contains all transitive dependency
              // artifacts of the project.  Each artifact in the map
              // is guaranteed to be resolved.

              @SuppressWarnings("unchecked")
              final Map<String, Artifact> artifactMap = project.getArtifactMap();
              if (artifactMap != null) {
                final Artifact pa = artifactMap.get(new StringBuilder(artifact.getGroupId()).append(":").append(artifact.getArtifactId()).toString());
                if (pa != null) {
                  artifact = pa;
                  assert artifact.isResolved();
                  assert artifact.getFile() != null;
                }
              }

              if (!artifact.isResolved()) {
                // Next, see if the project's artifact itself "is" the
                // current artifact.  The project's artifact is
                // guaranteed to be resolved.
                if (projectArtifact.getGroupId().equals(artifact.getGroupId()) &&
                    projectArtifact.getArtifactId().equals(artifact.getArtifactId())) {
                  artifact = projectArtifact;
                  assert artifact.isResolved();
                  assert artifact.getFile() != null;
                }
              }

              if (!artifact.isResolved()) {
                // Finally, perform manual artifact resolution.
                final ArtifactResolutionRequest request = new ArtifactResolutionRequest();
                request.setArtifact(artifact);
                request.setLocalRepository(localRepository);
                @SuppressWarnings("unchecked")
                final List<ArtifactRepository> remoteRepositories = project.getRemoteArtifactRepositories();
                request.setRemoteRepositories(remoteRepositories);

                final ArtifactResolutionResult result = resolver.resolve(request);
                if (result == null || !result.isSuccess()) {
                  new DefaultResolutionErrorHandler().throwErrors(request, result);
                } else {
                  @SuppressWarnings("unchecked")
                  final Collection<? extends Artifact> resolvedArtifacts = (Set<? extends Artifact>)result.getArtifacts();
                  assert resolvedArtifacts != null;
                  assert resolvedArtifacts.size() == 1;
                  artifact = resolvedArtifacts.iterator().next();
                  assert artifact != null;
                  assert artifact.isResolved();
                  assert artifact.getFile() != null;
                }
              }
            }

            assert artifact != null;
            assert artifact.isResolved();
            returnValue.add(artifact);
          }
        }
      }
      if (!returnValue.isEmpty()) {
        Collections.reverse(returnValue);
      }
    }
    return returnValue;
  }

}
