/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.rendering.internal.resolver;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Provider;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.internal.ContextComponentManagerProvider;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.model.EntityType;
import org.xwiki.model.internal.reference.DefaultReferenceAttachmentReferenceResolver;
import org.xwiki.model.internal.reference.DefaultReferenceDocumentReferenceResolver;
import org.xwiki.model.internal.reference.DefaultReferenceEntityReferenceResolver;
import org.xwiki.model.internal.reference.DefaultStringAttachmentReferenceResolver;
import org.xwiki.model.internal.reference.DefaultStringDocumentReferenceResolver;
import org.xwiki.model.internal.reference.DefaultStringEntityReferenceResolver;
import org.xwiki.model.internal.reference.DefaultStringSpaceReferenceResolver;
import org.xwiki.model.internal.reference.RelativeStringEntityReferenceResolver;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceProvider;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.rendering.listener.reference.AttachmentResourceReference;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.listener.reference.SpaceResourceReference;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Validate {@link DefaultResourceReferenceEntityReferenceResolver}.
 *
 * @version $Id$
 */
@ComponentList({ DefaultResourceReferenceEntityReferenceResolver.class,
    AttachmentResourceReferenceEntityReferenceResolver.class, DocumentResourceReferenceEntityReferenceResolver.class,
    DefaultStringAttachmentReferenceResolver.class, DefaultStringDocumentReferenceResolver.class,
    SpaceResourceReferenceEntityReferenceResolver.class, DefaultReferenceEntityReferenceResolver.class,
    DefaultStringEntityReferenceResolver.class, RelativeStringEntityReferenceResolver.class,
    DefaultReferenceAttachmentReferenceResolver.class, DefaultReferenceDocumentReferenceResolver.class,
    DefaultStringSpaceReferenceResolver.class, ContextComponentManagerProvider.class })

public class DefaultResourceReferenceEntityReferenceResolverTest
{
    private static final String DEFAULT_PAGE = "defaultpage";

    private static final String CURRENT_PAGE = "currentpage";

    private static final String CURRENT_SPACE = "currentspace";

    private static final String CURRENT_SUBSPACE = "currentsubspace";

    private static final String CURRENT_WIKI = "currentwiki";

    private static final String WIKI = "Wiki";

    private static final String SPACE = "Space";

    private static final String PAGE = "Page";

    private static final String ATTACHMENT = "file.ext";

    private static final DocumentReference CURRENT_DOCUMENT_REFERENCE =
        new DocumentReference(CURRENT_WIKI, CURRENT_SPACE, CURRENT_PAGE);

    @Rule
    public MockitoComponentMockingRule<EntityReferenceResolver<ResourceReference>> mocker =
        new MockitoComponentMockingRule<EntityReferenceResolver<ResourceReference>>(
            DefaultResourceReferenceEntityReferenceResolver.class);

    private EntityReferenceResolver<String> currentEntityReferenceResolver;

    private Provider<DocumentReference> currentDocumentProvider;

    private DocumentAccessBridge bridge;

    private EntityReferenceProvider defaultEntityProvider;

    private final Set<DocumentReference> existingDocuments = new HashSet<>();

    @Before
    public void before() throws Exception
    {
        this.currentEntityReferenceResolver =
            this.mocker.registerMockComponent(EntityReferenceResolver.TYPE_STRING, "current");

        this.currentDocumentProvider = this.mocker.registerMockComponent(
            new DefaultParameterizedType(null, Provider.class, DocumentReference.class), "current");
        when(this.currentDocumentProvider.get())
            .thenReturn(new DocumentReference(CURRENT_WIKI, CURRENT_SPACE, CURRENT_PAGE));

        this.bridge = this.mocker.registerMockComponent(DocumentAccessBridge.class);
        when(this.bridge.exists(any(DocumentReference.class))).then(new Answer<Boolean>()
        {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable
            {
                return existingDocuments.contains(invocation.getArguments()[0]);
            }
        });

        this.defaultEntityProvider = this.mocker.registerMockComponent(EntityReferenceProvider.class);
        when(this.defaultEntityProvider.getDefaultReference(EntityType.DOCUMENT))
            .thenReturn(new EntityReference(DEFAULT_PAGE, EntityType.DOCUMENT));
    }

    private DocumentResourceReference documentResource(String referenceString, boolean typed)
    {
        DocumentResourceReference reference = new DocumentResourceReference(referenceString);

        reference.setTyped(typed);

        return reference;
    }

    private SpaceResourceReference spaceResource(String referenceString, boolean typed)
    {
        SpaceResourceReference reference = new SpaceResourceReference(referenceString);

        reference.setTyped(typed);

        return reference;
    }

    private AttachmentResourceReference attachmentResource(String referenceString, boolean typed)
    {
        AttachmentResourceReference reference = new AttachmentResourceReference(referenceString);

        reference.setTyped(typed);

        return reference;
    }
    // Tests

    @Test
    public void resolve() throws ComponentLookupException
    {
        assertNull(this.mocker.getComponentUnderTest().resolve(null, null));
        assertNull(this.mocker.getComponentUnderTest().resolve(new ResourceReference("path", ResourceType.PATH), null));
    }

    @Test
    public void resolveTypeDocument() throws ComponentLookupException
    {
        assertEquals(new DocumentReference(WIKI, SPACE, PAGE),
            this.mocker.getComponentUnderTest().resolve(documentResource(WIKI + ':' + SPACE + '.' + PAGE, true), null));

        assertEquals(new DocumentReference(CURRENT_WIKI, SPACE, PAGE),
            this.mocker.getComponentUnderTest().resolve(documentResource(SPACE + '.' + PAGE, true), null));

        assertEquals(new DocumentReference(CURRENT_WIKI, CURRENT_SPACE, PAGE),
            this.mocker.getComponentUnderTest().resolve(documentResource(PAGE, true), null));
    }

    @Test
    public void resolveUntypeDocument() throws ComponentLookupException
    {
        // When the page does not exist

        assertEquals(new DocumentReference(WIKI, Arrays.asList(SPACE, PAGE), DEFAULT_PAGE), this.mocker
            .getComponentUnderTest().resolve(documentResource(WIKI + ':' + SPACE + '.' + PAGE, false), null));
        assertEquals(new DocumentReference(CURRENT_WIKI, Arrays.asList(SPACE, PAGE), DEFAULT_PAGE),
            this.mocker.getComponentUnderTest().resolve(documentResource(SPACE + '.' + PAGE, false), null));
        assertEquals(new DocumentReference(CURRENT_WIKI, Arrays.asList(CURRENT_SPACE, PAGE), DEFAULT_PAGE),
            this.mocker.getComponentUnderTest().resolve(documentResource(PAGE, false), null));

        // Already ends with default page name

        assertEquals(new DocumentReference(WIKI, SPACE, DEFAULT_PAGE), this.mocker.getComponentUnderTest()
            .resolve(documentResource(WIKI + ':' + SPACE + '.' + DEFAULT_PAGE, false), null));
        assertEquals(new DocumentReference(CURRENT_WIKI, SPACE, DEFAULT_PAGE),
            this.mocker.getComponentUnderTest().resolve(documentResource(SPACE + '.' + DEFAULT_PAGE, false), null));
        assertEquals(new DocumentReference(CURRENT_WIKI, CURRENT_SPACE, DEFAULT_PAGE),
            this.mocker.getComponentUnderTest().resolve(documentResource(DEFAULT_PAGE, false), null));

        // When the page exist

        this.existingDocuments.add(new DocumentReference(WIKI, SPACE, PAGE));
        assertEquals(new DocumentReference(WIKI, SPACE, PAGE), this.mocker.getComponentUnderTest()
            .resolve(documentResource(WIKI + ':' + SPACE + '.' + PAGE, false), null));
        this.existingDocuments.add(new DocumentReference(CURRENT_WIKI, SPACE, PAGE));
        assertEquals(new DocumentReference(CURRENT_WIKI, SPACE, PAGE),
            this.mocker.getComponentUnderTest().resolve(documentResource(SPACE + '.' + PAGE, false), null));
        this.existingDocuments.add(new DocumentReference(CURRENT_WIKI, CURRENT_SPACE, PAGE));
        assertEquals(new DocumentReference(CURRENT_WIKI, CURRENT_SPACE, PAGE),
            this.mocker.getComponentUnderTest().resolve(documentResource(PAGE, false), null));

        // When the page is current page

        assertEquals(new DocumentReference(CURRENT_WIKI, CURRENT_SPACE, CURRENT_PAGE),
            this.mocker.getComponentUnderTest().resolve(documentResource(CURRENT_PAGE, false), null));

        assertEquals(new DocumentReference(CURRENT_WIKI, CURRENT_SPACE, CURRENT_PAGE),
            this.mocker.getComponentUnderTest().resolve(documentResource("", false), null));
    }

    @Test
    public void resolveUntypeDocumentWhenCurrentPageIsSpace() throws ComponentLookupException
    {
        // Current is top level space

        when(this.currentDocumentProvider.get())
            .thenReturn(new DocumentReference(CURRENT_WIKI, CURRENT_SPACE, DEFAULT_PAGE));

        assertEquals(new DocumentReference(CURRENT_WIKI, PAGE, DEFAULT_PAGE),
            this.mocker.getComponentUnderTest().resolve(documentResource(PAGE, false), null));

        assertEquals(new DocumentReference(CURRENT_WIKI, Arrays.asList(SPACE, PAGE), DEFAULT_PAGE),
            this.mocker.getComponentUnderTest().resolve(documentResource(SPACE + '.' + PAGE, false), null));

        // Current is subspace

        // When sibling page does not exist

        when(this.currentDocumentProvider.get()).thenReturn(
            new DocumentReference(CURRENT_WIKI, Arrays.asList(CURRENT_SPACE, CURRENT_SUBSPACE), DEFAULT_PAGE));

        assertEquals(new DocumentReference(CURRENT_WIKI, Arrays.asList(CURRENT_SPACE, PAGE), DEFAULT_PAGE),
            this.mocker.getComponentUnderTest().resolve(documentResource(PAGE, false), null));

        assertEquals(new DocumentReference(CURRENT_WIKI, Arrays.asList(SPACE, PAGE), DEFAULT_PAGE),
            this.mocker.getComponentUnderTest().resolve(documentResource(SPACE + '.' + PAGE, false), null));

        // When sibling page exist

        this.existingDocuments.add(new DocumentReference(CURRENT_WIKI, CURRENT_SPACE, PAGE));

        assertEquals(new DocumentReference(CURRENT_WIKI, CURRENT_SPACE, PAGE),
            this.mocker.getComponentUnderTest().resolve(documentResource(PAGE, false), null));

        assertEquals(new DocumentReference(CURRENT_WIKI, Arrays.asList(SPACE, PAGE), DEFAULT_PAGE),
            this.mocker.getComponentUnderTest().resolve(documentResource(SPACE + '.' + PAGE, false), null));

    }

    @Test
    public void resolveTypeSpace() throws ComponentLookupException
    {
        assertEquals(new SpaceReference(WIKI, SPACE),
            this.mocker.getComponentUnderTest().resolve(spaceResource(WIKI + ':' + SPACE, true), null));

        assertEquals(new SpaceReference(CURRENT_WIKI, SPACE),
            this.mocker.getComponentUnderTest().resolve(spaceResource(SPACE, true), null));
    }

    @Test
    public void resolveTypeAttachment() throws ComponentLookupException
    {
        // When the page does not exist

        assertEquals(
            new AttachmentReference(ATTACHMENT, new DocumentReference(WIKI, Arrays.asList(SPACE, PAGE), DEFAULT_PAGE)),
            this.mocker.getComponentUnderTest()
                .resolve(attachmentResource(WIKI + ':' + SPACE + '.' + PAGE + '@' + ATTACHMENT, true), null));

        assertEquals(
            new AttachmentReference(ATTACHMENT,
                new DocumentReference(CURRENT_WIKI, Arrays.asList(SPACE, PAGE), DEFAULT_PAGE)),
            this.mocker.getComponentUnderTest().resolve(attachmentResource(SPACE + '.' + PAGE + '@' + ATTACHMENT, true),
                null));

        assertEquals(
            new AttachmentReference(ATTACHMENT,
                new DocumentReference(CURRENT_WIKI, Arrays.asList(CURRENT_SPACE, PAGE), DEFAULT_PAGE)),
            this.mocker.getComponentUnderTest().resolve(attachmentResource(PAGE + '@' + ATTACHMENT, true), null));

        // When the page exist

        this.existingDocuments.add(new DocumentReference(WIKI, SPACE, PAGE));
        assertEquals(new AttachmentReference(ATTACHMENT, new DocumentReference(WIKI, SPACE, PAGE)),
            this.mocker.getComponentUnderTest()
                .resolve(attachmentResource(WIKI + ':' + SPACE + '.' + PAGE + '@' + ATTACHMENT, true), null));

        this.existingDocuments.add(new DocumentReference(CURRENT_WIKI, SPACE, PAGE));
        assertEquals(new AttachmentReference(ATTACHMENT, new DocumentReference(CURRENT_WIKI, SPACE, PAGE)), this.mocker
            .getComponentUnderTest().resolve(attachmentResource(SPACE + '.' + PAGE + '@' + ATTACHMENT, true), null));

        this.existingDocuments.add(new DocumentReference(CURRENT_WIKI, CURRENT_SPACE, PAGE));
        assertEquals(new AttachmentReference(ATTACHMENT, new DocumentReference(CURRENT_WIKI, CURRENT_SPACE, PAGE)),
            this.mocker.getComponentUnderTest().resolve(attachmentResource(PAGE + '@' + ATTACHMENT, true), null));

        // When page is current page

        assertEquals(
            new AttachmentReference(ATTACHMENT, new DocumentReference(CURRENT_WIKI, CURRENT_SPACE, CURRENT_PAGE)),
            this.mocker.getComponentUnderTest().resolve(attachmentResource(ATTACHMENT, true), null));

        this.existingDocuments.add(new DocumentReference(CURRENT_WIKI, CURRENT_SPACE, CURRENT_PAGE));
        assertEquals(
            new AttachmentReference(ATTACHMENT, new DocumentReference(CURRENT_WIKI, CURRENT_SPACE, CURRENT_PAGE)),
            this.mocker.getComponentUnderTest().resolve(attachmentResource(ATTACHMENT, true), null));
    }
}