/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.gen5.commons.meta.API;

/**
 * Mutable descriptor of a test or container that has been discovered by a
 * {@link TestEngine}.
 *
 * @see TestEngine
 * @since 5.0
 */
@API(Experimental)
public interface TestDescriptor {

	/**
	 * Get the unique identifier (UID) for the described test.
	 *
	 * <p>Uniqueness must be guaranteed across an entire test plan,
	 * regardless of how many engines are used behind the scenes.
	 *
	 * @return the {@code UniqueId} for this descriptor; never {@code null}
	 */
	UniqueId getUniqueId();

	/**
	 * Get the display name of the represented test or container.
	 *
	 * <p>A <em>display name</em> is a human-readable name for a test or
	 * container that is typically used for test reporting in IDEs and build
	 * tools. Display names may contain spaces, special characters, and emoji,
	 * and the format may be customized by {@link TestEngine TestEngines} or
	 * potentially by end users as well. Consequently, display names should
	 * never be parsed; rather, they should be used for display purposes only.
	 *
	 * @return the display name for this descriptor; never {@code null} or empty
	 * @see #getSource()
	 */
	String getDisplayName();

	/**
	 * Get the {@linkplain TestSource source} of the represented test
	 * or container, if available.
	 *
	 * @see TestSource
	 */
	Optional<TestSource> getSource();

	/**
	 * Get the <em>parent</em> of the represented test or container, if
	 * available.
	 */
	Optional<TestDescriptor> getParent();

	/**
	 * Set the <em>parent</em> of the represented test or container.
	 *
	 * @param parent the new parent of this descriptor; may be {@code null}.
	 */
	void setParent(TestDescriptor parent);

	/**
	 * Determine if this descriptor represents a test.
	 */
	boolean isTest();

	/**
	 * Determine if this descriptor represents a container.
	 */
	boolean isContainer();

	/**
	 * Determine if this descriptor is a <em>root</em> descriptor.
	 *
	 * <p>Root descriptor are descriptors without a parent.
	 */
	default boolean isRoot() {
		return !getParent().isPresent();
	}

	/**
	 * Get the set of {@linkplain TestTag tags} of this descriptor.
	 *
	 * @see TestTag
	 */
	Set<TestTag> getTags();

	/**
	 * Get the set of <em>children</em> of this descriptor.
	 */
	Set<? extends TestDescriptor> getChildren();

	/**
	 * Add a <em>child</em> to this descriptor.
	 *
	 * @param descriptor the child to add to this descriptor; must not be
	 * {@code null}.
	 */
	void addChild(TestDescriptor descriptor);

	/**
	 * Remove a <em>child</em> from this descriptor.
	 *
	 * @param descriptor the child to remove from this descriptor; must not be
	 * {@code null}.
	 */
	void removeChild(TestDescriptor descriptor);

	/**
	 * Remove this descriptor from its parent and removes all the children from
	 * this descriptor.
	 */
	void removeFromHierarchy();

	/**
	 * Get the set of <em>descendants</em> of this descriptor.
	 *
	 * <p>A <em>descendant</em> is a child of this descriptor or a child of one of
	 * its children, recursively.
	 */
	default Set<? extends TestDescriptor> allDescendants() {
		Set<TestDescriptor> all = new LinkedHashSet<>();
		all.addAll(getChildren());
		for (TestDescriptor child : getChildren()) {
			all.addAll(child.allDescendants());
		}
		return all;
	}

	/**
	 * Determine if this descriptor or any of its descendants represents a test.
	 */
	default boolean hasTests() {
		return (isTest() || getChildren().stream().anyMatch(TestDescriptor::hasTests));
	}

	/**
	 * Find this descriptor or any of its descendants by the supplied unique ID.
	 */
	Optional<? extends TestDescriptor> findByUniqueId(UniqueId uniqueId);

	/**
	 * Visitor for the tree-like {@link TestDescriptor} structure.
	 *
	 * @see TestDescriptor#accept
	 */
	interface Visitor {

		/**
		 * Visit a {@link TestDescriptor}.
		 */
		void visit(TestDescriptor descriptor);
	}

	/**
	 * Accept a visitor to the subtree starting with this descriptor.
	 */
	default void accept(Visitor visitor) {
		visitor.visit(this);
		// Create a copy of the set in order to avoid a ConcurrentModificationException
		new LinkedHashSet<>(this.getChildren()).forEach(child -> child.accept(visitor));
	}

}
