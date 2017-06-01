/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.stunner.svg.gen.translator.css;

import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.steadystate.css.dom.CSSStyleRuleImpl;
import com.steadystate.css.dom.CSSStyleSheetImpl;
import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS3;
import org.apache.commons.lang3.StringUtils;
import org.kie.workbench.common.stunner.svg.gen.exception.TranslatorException;
import org.kie.workbench.common.stunner.svg.gen.model.StyleDefinition;
import org.kie.workbench.common.stunner.svg.gen.model.TransformDefinition;
import org.kie.workbench.common.stunner.svg.gen.model.impl.StyleDefinitionImpl;
import org.kie.workbench.common.stunner.svg.gen.model.impl.TransformDefinitionImpl;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleDeclaration;

public class SVGStyleTranslatorHelper {

    private static final String TRANSFORM_SCALE = "scale";
    private static final String TRANSFORM_TRANSLATE = "translate";
    private static final Pattern TRANSFORM_PATTERN = Pattern.compile("(.*)\\((.*),(.*)\\)");

    public static final String OPACITY = "opacity";
    public static final String FILL = "fill";
    public static final String FILL_OPACITY = "fill-opacity";
    public static final String STROKE = "stroke";
    public static final String STROKE_OPACITY = "stroke-opacity";
    public static final String STROKE_WIDTH = "stroke-width";
    public static final String STYLE = "style";
    public static final String TRANSFORM = "transform";
    public static final String ATTR_VALUE_NONE = "none";

    public static final String[] ATTR_NAMES = new String[]{
            OPACITY, FILL, FILL_OPACITY, STROKE, STROKE_OPACITY, STROKE_WIDTH
    };

    public static TransformDefinition parseTransformDefinition(final Element element) throws TranslatorException {
        final String transformRaw = element.getAttribute(TRANSFORM);
        if (!isEmpty(transformRaw)) {
            final double[] t = parseTransform(transformRaw);
            return new TransformDefinitionImpl(t[0],
                                               t[1],
                                               t[2],
                                               t[3]);
        }
        return new TransformDefinitionImpl();
    }

    private static double[] parseTransform(final String raw) throws TranslatorException {
        double sx = 1;
        double sy = 1;
        double tx = 0;
        double ty = 0;
        final String[] split = raw.split(" ");
        for (final String transformDec : split) {
            final Matcher m = TRANSFORM_PATTERN.matcher(transformDec);
            if (m.matches()) {
                final String op = m.group(1).trim();
                final String x = m.group(2).trim();
                final String y = m.group(3).trim();
                switch (op) {
                    case TRANSFORM_SCALE:
                        sx = SVGAttributeParserUtils.toPixelValue(x);
                        sy = SVGAttributeParserUtils.toPixelValue(y);
                        break;
                    case TRANSFORM_TRANSLATE:
                        tx = SVGAttributeParserUtils.toPixelValue(x);
                        ty = SVGAttributeParserUtils.toPixelValue(y);
                        break;
                }
            } else {
                throw new TranslatorException("Unrecognized transform attribute value format [" + raw + "]");
            }
        }
        return new double[]{sx, sy, tx, ty};
    }

    // For now only single declaration support, the first one found.
    public static StyleDefinition parseStyleDefinition(final Element element) throws TranslatorException {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < ATTR_NAMES.length; i++) {
            final String key = ATTR_NAMES[i];
            final String value = element.getAttribute(key);
            if (!isEmpty(value)) {
                builder.append(key).append(": ").append(value).append("; ");
            }
        }
        final String styleRaw = element.getAttribute(STYLE);
        if (!isEmpty(styleRaw)) {
            builder.append(styleRaw);
        }
        if (0 < builder.length()) {
            return parseStyleDefinition(builder.toString());
        }
        // Return default styles.
        return new StyleDefinitionImpl.Builder().build();
    }

    public static StyleDefinition parseStyleDefinition(final String styleRaw) throws TranslatorException {
        final CSSStyleSheetImpl sheet = parseStyleSheet(styleRaw);
        final CSSRuleList cssRules = sheet.getCssRules();
        for (int i = 0; i < cssRules.getLength(); i++) {
            final CSSRule item = cssRules.item(i);
            if (CSSRule.STYLE_RULE == item.getType()) {
                final CSSStyleRuleImpl rule = (CSSStyleRuleImpl) item;
                String selectorText = rule.getSelectorText();
                final CSSStyleDeclaration declaration = rule.getStyle();
                return parseStyleDefinition(declaration);
            }
        }
        return null;
    }

    private static CSSStyleSheetImpl parseStyleSheet(final String style) throws TranslatorException {
        try {
            final String declaration = ".shape { " + style + "}";
            InputSource source = new InputSource(new StringReader(declaration));
            CSSOMParser parser = new CSSOMParser(new SACParserCSS3());
            return (CSSStyleSheetImpl) parser.parseStyleSheet(source,
                                                              null,
                                                              null);
        } catch (final IOException e) {
            throw new TranslatorException("Exception while parsing some style defintion.",
                                          e);
        }
    }

    private static StyleDefinition parseStyleDefinition(final CSSStyleDeclaration declaration) {
        final StyleDefinitionImpl.Builder builder = new StyleDefinitionImpl.Builder();
        boolean isFillNone = false;
        boolean isStrokeNone = false;
        for (int j = 0; j < declaration.getLength(); j++) {
            final String property = declaration.item(j).trim();
            final String value = declaration.getPropertyValue(property).trim();
            switch (property) {
                case OPACITY:
                    builder.setAlpha(SVGAttributeParserUtils.toPixelValue(value));
                    break;
                case FILL:
                    if (ATTR_VALUE_NONE.equals(value)) {
                        isFillNone = true;
                    } else {
                        builder.setFillColor(SVGAttributeParserUtils.toHexColorString(value));
                    }
                    break;
                case FILL_OPACITY:
                    builder.setFillAlpha(SVGAttributeParserUtils.toPixelValue(value));
                    break;
                case STROKE:
                    if (ATTR_VALUE_NONE.equals(value)) {
                        isStrokeNone = true;
                    } else {
                        builder.setStrokeColor(SVGAttributeParserUtils.toHexColorString(value));
                    }
                    break;
                case STROKE_OPACITY:
                    builder.setStrokeAlpha(SVGAttributeParserUtils.toPixelValue(value));
                    break;
                case STROKE_WIDTH:
                    builder.setStrokeWidth(SVGAttributeParserUtils.toPixelValue(value));
                    break;
            }
        }
        if (isFillNone) {
            builder.setFillAlpha(0);
        }
        if (isStrokeNone) {
            builder.setStrokeAlpha(0);
        }
        return builder.build();
    }

    private static boolean isEmpty(final String s) {
        return StringUtils.isEmpty(s);
    }
}
