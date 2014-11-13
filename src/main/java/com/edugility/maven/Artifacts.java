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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.maven.artifact.Artifact;

import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.DefaultResolutionErrorHandler;

import org.apache.maven.artifact.resolver.filter.ArtifactFilter;

import org.apache.maven.artifact.repository.ArtifactRepository;

import org.apache.maven.project.MavenProject;

import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;

import org.apache.maven.shared.dependency.graph.traversal.CollectingDependencyNodeVisitor;

/**
 * A utility class for working with {@link Artifact}s.
 *
 * @author <a href="http://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see Artifact
 *
 * @see #getArtifactsInTopologicalOrder(MavenProject,
 * DependencyGraphBuilder, ArtifactFilter, ArtifactResolver,
 * ArtifactRepository)
 */
public class Artifacts {
  
  /**
   * Creates a new {@link Artifact}s instance.
   */
  public Artifacts() {
    super();
  }

  /**
   * Returns a {@link Logger} suitable for this {@link Artifacts}
   * class.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * <p>The default implementation returns the result of invoking
   * {@link Logger#getLogger(String)
   * Logger.getLogger(this.getClass().getName())}.</p>
   *
   * @return a non-{@code null} {@link Logger}
   */
  protected Logger getLogger() {
    return Logger.getLogger(this.getClass().getName());
  }

  /**
   * Returns an unmodifiable, non-{@code null} {@link Collection} of
   * {@link Artifact}s, each element of which is a non-{@code null}
   * {@link Artifact} that represents either the supplied {@link
   * MavenProject} itself or a {@linkplain MavenProject#getArtifacts()
   * dependency of it}.
   *
   * <p>The returned {@link Artifact} {@link Collection} will be
   * sorted in topological order, from an {@link Artifact} with no
   * dependencies as the first element, to the {@link Artifact}
   * representing the {@link MavenProject} itself as the last.</p>
   *
   * <p>All {@link Artifact}s that are not {@linkplain
   * Object#equals(Object) equal to} the return value of {@link
   * MavenProject#getArtifact() project.getArtifact()} will be
   * {@linkplain ArtifactResolver#resolve(ArtifactResolutionRequest)
   * resolved} if they are not already {@linkplain
   * Artifact#isResolved() resolved}.  No guarantee of {@linkplain
   * Artifact#isResolved() resolution status} is made of the
   * {@linkplain MavenProject#getArtifact() project
   * <code>Artifact</code>}, which in normal&mdash;possibly
   * all?&mdash;cases will be unresolved.</p>
   *
   * @param project the {@link MavenProject} for which resolved {@link
   * Artifact}s should be returned; must not be {@code null}
   *
   * @param dependencyGraphBuilder the {@link DependencyGraphBuilder}
   * instance that will calculate the dependency graph whose postorder
   * traversal will yield the {@link Artifact}s to be resolved; must
   * not be {@code null}
   *
   * @param filter an {@link ArtifactFilter} used during {@linkplain
   * DependencyGraphBuilder#buildDependencyGraph(MavenProject,
   * ArtifactFilter) dependency graph assembly}; may be {@code null}
   *
   * @param resolver an {@link ArtifactResolver} that will use the <a
   * href="http://maven.apache.org/ref/3.0.5/maven-compat/apidocs/src-html/org/apache/maven/artifact/resolver/DefaultArtifactResolver.html#line.335">Maven
   * 3.0.5 <code>Artifact</code> resolution algorithm</a> to resolve
   * {@link Artifact}s returned by the {@link
   * DependencyGraphBuilder#buildDependencyGraph(MavenProject,
   * ArtifactFilter)} method; must not be {@code null}
   *
   * @param localRepository an {@link ArtifactRepository} representing
   * the local Maven repository in effect; may be {@code null}
   *
   * @return a non-{@code null}, {@linkplain
   * Collections#unmodifiableCollection(Collection) unmodifiable}
   * {@link Collection} of non-{@code null} {@link Artifact} instances
   *
   * @exception IllegalArgumentException if {@code project}, {@code
   * dependencyGraphBuilder} or {@code resolver} is {@code null}
   *
   * @exception ArtifactResolutionException if there were problems
   * {@linkplain ArtifactResolver#resolve(ArtifactResolutionRequest)
   * resolving} {@link Artifact} instances
   *
   * @exception DependencyGraphBuilderException if there were problems
   * {@linkplain
   * DependencyGraphBuilder#buildDependencyGraph(MavenProject,
   * ArtifactFilter) building the dependency graph}
   *
   * @see ArtifactResolver#resolve(ArtifactResolutionRequest)
   *
   * @see DependencyGraphBuilder#buildDependencyGraph(MavenProject,
   * ArtifactFilter)
   */
  public Collection<? extends Artifact> getArtifactsInTopologicalOrder(final MavenProject project,
                                                                       final DependencyGraphBuilder dependencyGraphBuilder, 
                                                                       final ArtifactFilter filter,
                                                                       final ArtifactResolver resolver,
                                                                       final ArtifactRepository localRepository)
    throws DependencyGraphBuilderException, ArtifactResolutionException {
    final Logger logger = this.getLogger();
    if (logger != null && logger.isLoggable(Level.FINER)) {
      logger.entering(this.getClass().getName(), "getArtifactsInTopologicalOrder", new Object[] { project, dependencyGraphBuilder, filter, resolver, localRepository });
    }
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

    final DependencyNode projectNode = dependencyGraphBuilder.buildDependencyGraph(project, filter);
    assert projectNode != null;
    final CollectingDependencyNodeVisitor visitor = new CollectingDependencyNodeVisitor();
    projectNode.accept(visitor);
    final Collection<? extends DependencyNode> nodes = visitor.getNodes();

    if (nodes == null || nodes.isEmpty()) {
      if (logger != null && logger.isLoggable(Level.FINE)) {
        logger.logp(Level.FINE, this.getClass().getName(), "getArtifactsInTopologicalOrder", "No dependency nodes encountered");
      }

    } else {
      final Artifact projectArtifact = project.getArtifact();

      returnValue = new ArrayList<Artifact>();

      for (final DependencyNode node : nodes) {
        if (node != null) {
          Artifact artifact = node.getArtifact();
          if (artifact != null) {

            if (!artifact.isResolved()) {
              if (logger != null && logger.isLoggable(Level.FINE)) {
                logger.logp(Level.FINE, this.getClass().getName(), "getArtifactsInTopologicalOrder", "Artifact {0} is unresolved", artifact);
              }

              if (artifact.equals(projectArtifact)) {
                // First see if the artifact is the project artifact.
                // If so, then it by definition won't be able to be
                // resolved, because it's being built.
                if (logger != null && logger.isLoggable(Level.FINE)) {
                  logger.logp(Level.FINE, this.getClass().getName(), "getArtifactsInTopologicalOrder", "Artifact {0} resolved to project artifact: {1}", new Object[] { artifact, projectArtifact });
                }
                artifact = projectArtifact;

              } else {
                // See if the project's associated artifact map
                // contains a resolved version of this artifact.  The
                // artifact map contains all transitive dependency
                // artifacts of the project.  Each artifact in the map
                // is guaranteed to be resolved.
                @SuppressWarnings("unchecked")
                final Map<String, Artifact> artifactMap = project.getArtifactMap();
                if (artifactMap != null && !artifactMap.isEmpty()) {
                  final Artifact mapArtifact = artifactMap.get(new StringBuilder(artifact.getGroupId()).append(":").append(artifact.getArtifactId()).toString());
                  if (mapArtifact != null) {
                    if (logger != null && logger.isLoggable(Level.FINE)) {
                      logger.logp(Level.FINE, this.getClass().getName(), "getArtifactsInTopologicalOrder", "Artifact {0} resolved from project artifact map: {1}", new Object[] { artifact, mapArtifact });
                    }
                    artifact = mapArtifact;
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

                  if (logger != null && logger.isLoggable(Level.FINE)) {
                    logger.logp(Level.FINE, this.getClass().getName(), "getArtifactsInTopologicalOrder", "Resolving artifact {0} using ArtifactResolutionRequest {1}", new Object[] { artifact, request });
                  }

                  final ArtifactResolutionResult result = resolver.resolve(request);
                  if (result == null || !result.isSuccess()) {
                    this.handleArtifactResolutionError(request, result);
                  } else {
                    @SuppressWarnings("unchecked")
                    final Collection<? extends Artifact> resolvedArtifacts = (Set<? extends Artifact>)result.getArtifacts();
                    if (resolvedArtifacts == null || resolvedArtifacts.isEmpty()) {
                      if (logger != null && logger.isLoggable(Level.WARNING)) {
                        logger.logp(Level.WARNING, this.getClass().getName(), "getArtifactsInTopologicalOrder", "Artifact resolution failed silently for artifact {0}", artifact);
                      }
                    } else {
                      final Artifact resolvedArtifact = resolvedArtifacts.iterator().next();
                      if (resolvedArtifact != null) {
                        assert resolvedArtifact.isResolved();
                        artifact = resolvedArtifact;
                      } else if (logger != null && logger.isLoggable(Level.WARNING)) {
                        logger.logp(Level.WARNING, this.getClass().getName(), "getArtifactsInTopologicalOrder", "Artifact resolution failed silently for artifact {0}", artifact);
                      }
                    }
                  }
                }

              }
            }

            if (artifact != null) {
              returnValue.add(artifact);
            }
          }
        }
      }
      if (!returnValue.isEmpty()) {
        Collections.reverse(returnValue);
      }
    }
    if (returnValue == null) {
      returnValue = Collections.emptyList();
    } else {
      returnValue = Collections.unmodifiableList(returnValue);
    }
    if (logger != null && logger.isLoggable(Level.FINER)) {
      logger.exiting(this.getClass().getName(), "getArtifactsInTopologicalOrder", returnValue);
    }
    return returnValue;
  }

  /**
   * Handles an error that is represented by the supplied {@link
   * ArtifactResolutionResult} that was issued in response to the
   * supplied {@link ArtifactResolutionRequest}.
   *
   * <p>This implementation performs the following operations:</p>
   *
   * <blockquote><pre>new {@link
   * DefaultResolutionErrorHandler#DefaultResolutionErrorHandler()
   * DefaultResolutionErrorHandler()}.{@link
   * DefaultResolutionErrorHandler#throwErrors(ArtifactResolutionRequest,
   * ArtifactResolutionResult) throwErrors(request,
   * result)};</pre></blockquote>
   *
   * @param request the {@link ArtifactResolutionRequest} that caused
   * the error; must not be {@code null}
   *
   * @param result the {@link ArtifactResolutionResult} that resulted;
   * must not be {@code null}
   *
   * @exception ArtifactResolutionException if this method is not
   * overridden to do something else
   *
   * @see DefaultResolutionErrorHandler
   *
   * @see
   * DefaultResolutionErrorHandler#throwErrors(ArtifactResolutionRequest,
   * ArtifactResolutionResult)
   */
  protected void handleArtifactResolutionError(final ArtifactResolutionRequest request, final ArtifactResolutionResult result) throws ArtifactResolutionException {
    new DefaultResolutionErrorHandler().throwErrors(request, result);
  }

}
