/*
 * Copyright (C) 2013 salesforce.com, inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.auraframework.impl.root.parser.handler;

import java.util.Collections;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.auraframework.adapter.ConfigAdapter;
import org.auraframework.adapter.DefinitionParserAdapter;
import org.auraframework.builder.DocumentationDefBuilder;
import org.auraframework.def.DefDescriptor;
import org.auraframework.def.DocumentationDef;
import org.auraframework.impl.documentation.DocumentationDefImpl;
import org.auraframework.pojo.Description;
import org.auraframework.pojo.Example;
import org.auraframework.pojo.Meta;
import org.auraframework.service.DefinitionService;
import org.auraframework.system.TextSource;
import org.auraframework.throwable.quickfix.QuickFixException;

public class DocumentationDefHandler extends FileTagHandler<DocumentationDef> {

    public static final String TAG = "aura:documentation";

    protected final static Set<String> ALLOWED_ATTRIBUTES = Collections.emptySet();

    private final DocumentationDefImpl.Builder builder = new DocumentationDefImpl.Builder();

    // counter used to index DescriptionDefs with no explicit id
    private int idCounter = 0;

    public DocumentationDefHandler() {
        super();
    }

    public DocumentationDefHandler(DefDescriptor<DocumentationDef> defDescriptor, TextSource<DocumentationDef> source,
                                   XMLStreamReader xmlReader, boolean isInInternalNamespace, DefinitionService definitionService,
                                   ConfigAdapter configAdapter, DefinitionParserAdapter definitionParserAdapter) {
        super(defDescriptor, source, xmlReader, isInInternalNamespace, definitionService, configAdapter, definitionParserAdapter);
        builder.setDescriptor(getDefDescriptor());
        builder.setLocation(getLocation());
        if (source != null) {
            builder.setOwnHash(source.getHash());
        }
        builder.setAccess(getAccess(isInInternalNamespace));
    }

    @Override
    public Set<String> getAllowedAttributes() {
        return ALLOWED_ATTRIBUTES;
    }

    @Override
    public String getHandledTag() {
        return TAG;
    }

    @Override
    public DocumentationDefBuilder getBuilder() {
        return builder;
    }

    @Override
    protected void handleChildTag() throws XMLStreamException, QuickFixException {
        String tag = getTagName();

        if (DescriptionDefHandler.TAG.equalsIgnoreCase(tag)) {
            Description desc = new DescriptionDefHandler(this,
                    xmlReader, source).getElement();
            builder.addDescription(desc.getName(), desc);

        } else if (ExampleDefHandler.TAG.equalsIgnoreCase(tag)) {
            Example ex = new ExampleDefHandler(this, xmlReader, source).getElement();
            builder.addExample(ex.getName(), ex);

        } else if (MetaDefHandler.TAG.equalsIgnoreCase(tag)) {
            Meta meta = new MetaDefHandler(this, xmlReader, source).getElement();
            builder.addMeta(meta.getName(), meta);
        } else {
            throw new XMLStreamException(String.format("<%s> cannot contain tag %s", getHandledTag(), tag));
        }
    }

    @Override
    protected void handleChildText() throws XMLStreamException, QuickFixException {
        String text = xmlReader.getText();
        if (!StringUtils.isBlank(text)) {
            throw new XMLStreamException(String.format(
                    "<%s> can contain only <aura:description> and <aura:example> tags.\nFound text: %s",
                    getHandledTag(), text));
        }
    }

    @Override
    protected void finishDefinition() {
    }

    @Override
    protected DocumentationDef createDefinition() throws QuickFixException {
        return builder.build();
    }

    String getNextId() {
        String ret = Integer.toString(idCounter);
        idCounter++;
        return ret;
    }
}
