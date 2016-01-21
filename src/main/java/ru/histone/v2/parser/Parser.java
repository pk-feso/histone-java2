package ru.histone.v2.parser;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import ru.histone.HistoneException;
import ru.histone.tokenizer.Token;
import static ru.histone.tokenizer.Tokens.*;
import static ru.histone.v2.parser.node.AstType.*;

import ru.histone.tokenizer.Tokens;
import ru.histone.v2.exceptions.SyntaxErrorException;
import ru.histone.v2.exceptions.UnexpectedTokenException;
import ru.histone.v2.parser.node.*;
import ru.histone.v2.parser.tokenizer.ExpressionList;
import ru.histone.v2.parser.tokenizer.Tokenizer;
import ru.histone.v2.parser.tokenizer.TokenizerResult;
import ru.histone.v2.parser.tokenizer.TokenizerWrapper;
import ru.histone.v2.utils.ParserUtils;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by alexey.nevinsky on 24.12.2015.
 */
public class Parser {
    private static final Pattern regexpFlagsPattern = Pattern.compile("^(?:([gim])(?!.*\\1))*$");
    public static final String IDENTIFIER = "IDENTIFIER";

    public ExpAstNode process(String template, String baseURI) throws HistoneException {
        Tokenizer tokenizer = new Tokenizer(template, baseURI, ExpressionList.VALUES);
        TokenizerWrapper wrapper = new TokenizerWrapper(tokenizer);
        ExpAstNode result = getNodeList(wrapper);
        if (!wrapper.next(T_EOF).isFound())
            throw buildUnexpectedTokenException(wrapper, "EOF");
        final Optimizer optimizer = new Optimizer();
        result = (ExpAstNode) optimizer.mergeStrings(result);
        final Marker marker = new Marker();
//        marker.markReferences(result);
        return result;
    }

    private ExpAstNode getNodeList(TokenizerWrapper wrapper) throws ParserException {
        ExpAstNode result = new ExpAstNode(AST_NODELIST);
        AstNode node;
        wrapper = new TokenizerWrapper(wrapper);
        for (; ; ) {
            node = getStatement(wrapper);
            if (node.getType() == AST_T_BREAK) {
                break;
            } else if (node.getType() != AST_T_NOP) {
                //todo check this
                if (node.getType() != AST_T_ARRAY) {
                    result.add(node);
                } else {
                    result.add(node);
                }
            }
        }
        return result;
    }

    private AstNode getStatement(TokenizerWrapper wrapper) throws ParserException {
        if (next(wrapper, T_BLOCK_START)) {
            return getTemplateStatement(wrapper);
        }
        if (next(wrapper, T_LITERAL_START)) {
            return getLiteralStatement(wrapper);
        }
        if (next(wrapper, T_CMT_START)) {
            return getCommentStatement(wrapper);
        }
        if (!wrapper.test(T_EOF.getId()).isFound()) {
            return new StringAstNode(wrapper.next().firstValue());
        }
        return new ExpAstNode(AST_T_BREAK);
    }

    private ExpAstNode getCommentStatement(TokenizerWrapper wrapper) {
        throw new NotImplementedException();
    }

    private AstNode getTemplateStatement(TokenizerWrapper wrapper) throws ParserException {
        wrapper = new TokenizerWrapper(wrapper, Collections.singletonList(T_SPACES.getId()));
        AstNode result;
        if (next(wrapper, T_IF)) {
            result = getIfStatement(wrapper);
        } else if (next(wrapper, T_FOR)) {
            result = getForStatement(wrapper);
        } else if (next(wrapper, T_VAR)) {
            result = getVarStatement(wrapper);
        } else if (next(wrapper, T_MACRO)) {
            result = getMacroStatement(wrapper);
        } else if (next(wrapper, T_RETURN)) {
            result = getReturnStatement(wrapper);
        } else if (next(wrapper, T_SUPRESS)) {
            result = getSupressStatement(wrapper);
        } else if (next(wrapper, T_LISTEN)) {
            result = getListenStatement(wrapper, AST_LISTEN);
        } else if (next(wrapper, T_TRIGGER)) {
            result = getListenStatement(wrapper, AST_TRIGGER);
        } else if (test(wrapper, T_SLASH, T_STATEMENT, T_BLOCK_END)) {
            result = new ExpAstNode(AST_T_BREAK);
        } else if (test(wrapper, T_STATEMENT)) {
            result = new ExpAstNode(AST_T_BREAK);
        } else {
            result = getExpressionStatement(wrapper);
        }
        return result;
    }

    private AstNode getExpressionStatement(TokenizerWrapper wrapper) throws ParserException {
        if (next(wrapper, T_BLOCK_END)) {
            return new ExpAstNode(AST_T_NOP);
        }
        AstNode expression = getExpression(wrapper);
        if (!next(wrapper, T_BLOCK_END)) {
            throw buildUnexpectedTokenException(wrapper, "}}");
        }
        return expression;
    }

    private ExpAstNode getListenStatement(TokenizerWrapper wrapper, AstType astListen) {
        throw new NotImplementedException();
    }

    private ExpAstNode getSupressStatement(TokenizerWrapper wrapper) {
        throw new NotImplementedException();
    }

    private ExpAstNode getReturnStatement(TokenizerWrapper wrapper) throws ParserException {
        final ExpAstNode result = new ExpAstNode(AST_RETURN);
        final TokenizerResult blockEnd = wrapper.next(T_BLOCK_END);
        if (next(wrapper, T_BLOCK_END)) {
            result.add(getNodesStatement(wrapper, false));
            if (!next(wrapper, T_SLASH, T_RETURN)) {
                throw buildUnexpectedTokenException(wrapper, "{{/return}}");
            }
        } else {
            result.add(getExpression(wrapper));
        }

        if (!next(wrapper, T_BLOCK_END)) {
            throw buildUnexpectedTokenException(wrapper, "}}");
        }

        return result;
    }

    private ExpAstNode getVarStatement(TokenizerWrapper wrapper) throws ParserException {
        TokenizerResult name;
        ExpAstNode result;
        if (!test(wrapper, T_ID, T_EQ)) {
            name = wrapper.next(T_ID);
            if (!name.isFound()) {
                throw buildUnexpectedTokenException(wrapper, IDENTIFIER);
            }
            if (!next(wrapper, T_BLOCK_END)) {
                throw buildUnexpectedTokenException(wrapper, "}}");
            }
            result = new ExpAstNode(AST_VAR);
            result.add(new StringAstNode(name.firstValue()));
            result.add(getNodesStatement(wrapper, false));
            if (!next(wrapper, T_SLASH, T_VAR)) {
                throw buildUnexpectedTokenException(wrapper, "{{/var}}");
            }
        } else {
            result = new ExpAstNode(AST_ARRAY);
            do {
                name = wrapper.next(T_ID);
                if (!name.isFound()) {
                    throw buildUnexpectedTokenException(wrapper, IDENTIFIER);
                }
                if (!next(wrapper, T_EQ)) {
                    throw buildUnexpectedTokenException(wrapper, "=");
                }
                ExpAstNode varNode = new ExpAstNode(AST_VAR)
                        .add(new StringAstNode(name.firstValue()))
                        .add(getExpression(wrapper));
                result.add(varNode);
                if (!next(wrapper, T_COMMA)) {
                    break;
                }
            } while (!wrapper.test(T_EOF.getId()).isFound());
        }
        if (!next(wrapper, T_BLOCK_END)) {
            throw buildUnexpectedTokenException(wrapper, "}}");
        }
        return result;
    }

    private ExpAstNode getNodesStatement(TokenizerWrapper wrapper, boolean nested) throws ParserException {
        ExpAstNode res = new ExpAstNode(AST_NODES);
        wrapper = new TokenizerWrapper(wrapper);
        AstNode node;
        for (; ; ) {
            if (nested && test(wrapper, T_BLOCK_END)) {
                break;
            }
            node = getStatement(wrapper);
            final AstType type = node.getType();
            if (type == AST_T_BREAK) {
                break;
            }
            if (type != AST_T_NOP) {
                if (type != AST_T_ARRAY) {
                    res.add(node);
                } else if (!node.hasValue()) {
                    final ExpAstNode expNode = (ExpAstNode) node;
                    res.addAll(expNode.getNodes());
                }
            }
        }
        if (nested && next(wrapper, T_BLOCK_END)) {
            throw buildUnexpectedTokenException(wrapper, "}}");
        }
        return res;
    }

    private ExpAstNode getForStatement(TokenizerWrapper wrapper) throws ParserException {
        final ExpAstNode node = new ExpAstNode(AST_FOR);
        final TokenizerResult id = wrapper.next(T_ID);
        if (id.isFound()) {
            if (next(wrapper, T_COLON)) {
                node.add(new StringAstNode(id.firstValue())); //add key name
                final TokenizerResult valueName = wrapper.next(T_ID);
                if (valueName.isFound()) {
                    node.add(new StringAstNode(valueName.firstValue())); //add value name
                } else {
                    throw buildUnexpectedTokenException(wrapper, IDENTIFIER);
                }
            } else {
                node
                        .add(new StringAstNode(null)) //add null as key name
                        .add(new StringAstNode(id.firstValue())); //add value name
            }
        } else {
            node
                    .add(new StringAstNode(null)) //add 'null' as key name
                    .add(new StringAstNode(null));//add 'null' as value name
        }

        if (!next(wrapper, T_IN)) {
            throw buildUnexpectedTokenException(wrapper, "in");
        }

        do {
            final AstNode node2 = getExpression(wrapper);
            if (!next(wrapper, T_BLOCK_END)) {
                throw buildUnexpectedTokenException(wrapper, "}}");
            }
            node.add(getNodeList(wrapper), node2);
        } while (next(wrapper, T_ELSEIF));

        if (next(wrapper, T_ELSE)) {
            if (!next(wrapper, T_BLOCK_END)) {
                throw buildUnexpectedTokenException(wrapper, "}}");
            }
            node.add(getNodeList(wrapper));
        }

        if (!next(wrapper, T_SLASH, T_FOR, T_BLOCK_END)) {
            throw buildUnexpectedTokenException(wrapper, "{{/for}}");
        }
        return node;
    }

    private ExpAstNode getIfStatement(TokenizerWrapper wrapper) throws ParserException {
        ExpAstNode node = new ExpAstNode(AST_IF);
        do {
            AstNode condition = getExpression(wrapper);
            if (!next(wrapper, T_BLOCK_END)) {
                throw buildUnexpectedTokenException(wrapper, "}}");
            }
            node.add(getNodesStatement(wrapper, false), condition);
        } while (next(wrapper, T_ELSEIF));

        if (next(wrapper, T_ELSE)) {
            if (!next(wrapper, T_BLOCK_END)) {
                throw buildUnexpectedTokenException(wrapper, "}}");
            }
            node.add(getNodesStatement(wrapper, false));
        }
        if (!next(wrapper, T_SLASH, T_IF, T_BLOCK_END)) {
            throw buildUnexpectedTokenException(wrapper, "{{/if}}");
        }

        return node;
    }

    private ExpAstNode getMacroStatement(TokenizerWrapper wrapper) throws ParserException {
        throw new NotImplementedException();
    }

    private AstNode getExpression(TokenizerWrapper wrapper) throws ParserException {
        if (test(wrapper, T_ARROW) ||
                test(wrapper, T_ID, T_ARROW) ||
                test(wrapper, T_LPAREN, T_RPAREN) ||
                test(wrapper, T_LPAREN, T_ID, T_COMMA) ||
                test(wrapper, T_LPAREN, T_ID, T_RPAREN, T_ARROW)) {
            return getMacroExpression(wrapper);
        }
        return getTernaryExpression(wrapper);
    }

    private ExpAstNode getMacroExpression(TokenizerWrapper wrapper) throws ParserException {
        final List<AstNode> varNodes = new ArrayList<>();

        TokenizerResult name = wrapper.next(T_ID);
        if (name.isFound()) {
            varNodes.add(ParserUtils.createNopNode(name.firstValue()));
        } else if (next(wrapper, T_LPAREN)) {
            if (!test(wrapper, T_RPAREN)) {
                do {
                    name = wrapper.next(T_ID);
                    if (name.isFound()) {
                        varNodes.add(ParserUtils.createNopNode(name.firstValue()));
                    } else {
                        throw buildUnexpectedTokenException(wrapper, IDENTIFIER);
                    }
                } while (next(wrapper, T_COMMA));
            }
            if (!next(wrapper, T_RPAREN)) {
                throw buildUnexpectedTokenException(wrapper, ")");
            }
        }

        if (!next(wrapper, T_ARROW)) {
            throw buildUnexpectedTokenException(wrapper, "=>");
        }

        if (varNodes.size() > 0) {
            varNodes.add(new LongAstNode(varNodes.size()));
        }

        return createMacroNode(wrapper, varNodes);
    }

    private ExpAstNode createMacroNode(
            TokenizerWrapper wrapper, List<AstNode> varNodes
    ) throws ParserException {
        final ExpAstNode returnNode = new ExpAstNode(AST_RETURN).add(getExpression(wrapper));
        final ExpAstNode listNode = new ExpAstNode(AST_NODELIST).add(returnNode);
        return new ExpAstNode(AST_MACRO).add(listNode).addAll(varNodes);
    }

    private AstNode getTernaryExpression(TokenizerWrapper wrapper) throws ParserException {
        AstNode res = getLogicalOrExpression(wrapper);
        while (next(wrapper, T_QUERY)) {
            ExpAstNode node = new ExpAstNode(AST_TERNARY);
            node.add(res, getExpression(wrapper));
            if (next(wrapper, T_COLON)) {
                node.add(getExpression(wrapper));
            }
            res = node;
        }
        return res;
    }

    private AstNode getLogicalOrExpression(TokenizerWrapper wrapper) throws ParserException {
        AstNode res = getLogicalAndExpression(wrapper);
        while (next(wrapper, T_OR)) {
            ExpAstNode node = new ExpAstNode(AST_OR);
            res = node.add(res).add(getLogicalAndExpression(wrapper));
        }
        return res;
    }

    private AstNode getLogicalAndExpression(TokenizerWrapper wrapper) throws ParserException {
        AstNode res = getBitwiseOrExpression(wrapper);
        while (next(wrapper, T_AND)) {
            ExpAstNode node = new ExpAstNode(AST_AND);
            res = node.add(res).add(getBitwiseOrExpression(wrapper));
        }
        return res;
    }

    private AstNode getBitwiseOrExpression(TokenizerWrapper wrapper) throws ParserException {
        AstNode res = getBitwiseXorExpression(wrapper);
        while (next(wrapper, T_BOR)) {
            ExpAstNode node = new ExpAstNode(AST_BOR);
            res = node.add(res).add(getBitwiseXorExpression(wrapper));
        }
        return res;
    }

    private AstNode getBitwiseXorExpression(TokenizerWrapper wrapper) throws ParserException {
        AstNode res = getBitwiseAndExpression(wrapper);
        while (next(wrapper, T_BXOR)) {
            ExpAstNode node = new ExpAstNode(AST_BXOR);
            res = node.add(res).add(getBitwiseAndExpression(wrapper));
        }
        return res;
    }

    private AstNode getBitwiseAndExpression(TokenizerWrapper wrapper) throws ParserException {
        AstNode res = getEqualityExpression(wrapper);
        while (next(wrapper, T_BAND)) {
            ExpAstNode node = new ExpAstNode(AST_BAND);
            res = node.add(res).add(getEqualityExpression(wrapper));
        }
        return res;
    }

    private AstNode getEqualityExpression(TokenizerWrapper wrapper) throws ParserException {
        AstNode res = getRelationalExpression(wrapper);
        while (test(wrapper, T_EQ) || test(wrapper, T_NEQ)) {
            ExpAstNode node;
            if (next(wrapper, T_EQ)) {
                node = new ExpAstNode(AST_EQ);
            } else {
                next(wrapper, T_NEQ);// we needed to read next token from buffer for right work
                node = new ExpAstNode(AST_NEQ);
            }
            res = node.add(res).add(getRelationalExpression(wrapper));
        }
        return res;
    }

    private AstNode getRelationalExpression(TokenizerWrapper wrapper) throws ParserException {
        AstNode res = getAdditiveExpression(wrapper);
        while (test(wrapper, T_LE)
                || test(wrapper, T_GE)
                || test(wrapper, T_LT)
                || test(wrapper, T_GT)
                ) {
            ExpAstNode node;
            if (next(wrapper, T_LE)) {
                node = new ExpAstNode(AST_LE);
            } else if (next(wrapper, T_GE)) {
                node = new ExpAstNode(AST_GE);
            } else if (next(wrapper, T_LT)) {
                node = new ExpAstNode(AST_LT);
            } else {
                next(wrapper, T_LT);// we needed to read next token from buffer for right work
                node = new ExpAstNode(AST_GT);
            }
            res = node.add(res).add(getAdditiveExpression(wrapper));
        }
        return res;
    }

    private AstNode getAdditiveExpression(TokenizerWrapper wrapper) throws ParserException {
        AstNode res = getMultiplicativeExpression(wrapper);
        while (test(wrapper, T_PLUS) || test(wrapper, T_MINUS)) {
            ExpAstNode node;
            if (next(wrapper, T_PLUS)) {
                node = new ExpAstNode(AST_ADD);
            } else {
                next(wrapper, T_MINUS);// we needed to read next token from buffer for right work
                node = new ExpAstNode(AST_SUB);
            }
            res = node.add(res).add(getMultiplicativeExpression(wrapper));
        }
        return res;
    }

    private AstNode getMultiplicativeExpression(TokenizerWrapper wrapper) throws ParserException {
        AstNode res = getUnaryExpression(wrapper);
        while (test(wrapper, T_STAR) || test(wrapper, T_SLASH) || test(wrapper, T_MOD)) {
            ExpAstNode node;
            if (next(wrapper, T_STAR)) {
                node = new ExpAstNode(AST_MUL);
            } else if (next(wrapper, T_SLASH)) {
                node = new ExpAstNode(AST_DIV);
            } else {
                next(wrapper, T_MOD);// we needed to read next token from buffer for right work
                node = new ExpAstNode(AST_MOD);
            }
            res = node.add(res).add(getUnaryExpression(wrapper));
        }
        return res;
    }

    private AstNode getUnaryExpression(TokenizerWrapper wrapper) throws ParserException {
        if (next(wrapper, T_NOT)) {
            AstNode node = getUnaryExpression(wrapper);
            return new ExpAstNode(AST_NOT).add(node);
        } else if (next(wrapper, T_MINUS)) {
            AstNode node = getUnaryExpression(wrapper);
            return new ExpAstNode(AST_USUB).add(node);
        } else {
            return getMemberExpression(wrapper);
        }
    }

    private AstNode getMemberExpression(TokenizerWrapper wrapper) throws ParserException {
        AstNode res = getPrimaryExpression(wrapper);

        while (true) {
            if (next(wrapper, T_DOT)) {
                res = new ExpAstNode(AST_PROP, res);
                if (wrapper.test(T_PROP.getId()) != null) {
                    throw buildUnexpectedTokenException(wrapper, IDENTIFIER);
                }
                //todo
//                res.(wrapper.next().first().getValue());
            } else if (next(wrapper, T_METHOD)) {
                res = new ExpAstNode(AST_METHOD, res);
                if (wrapper.test(T_PROP.getId()) != null) {
                    throw buildUnexpectedTokenException(wrapper, IDENTIFIER);
                }
                //todo
//                res.setValue(wrapper.next().first().getValue());
            } else if (next(wrapper, T_LBRACKET)) {
                res = new ExpAstNode(AST_PROP, res);
                //todo
//                res.add(getExpression(wrapper));
                if (wrapper.test(T_RBRACKET.getId()) != null) {
                    throw buildUnexpectedTokenException(wrapper, "]");
                }
            } else if (next(wrapper, T_LPAREN)) {
                res = new ExpAstNode(AST_CALL, res);
                if (wrapper.next(T_RPAREN) != null) {
                    continue;
                }
                do {
                    //todo
//                    res.add(getExpression(wrapper));
                } while (next(wrapper, T_COMMA));
                if (wrapper.test(T_RPAREN.getId()) != null) {
                    throw buildUnexpectedTokenException(wrapper, "]");
                }
            } else {
                return res;
            }
        }
    }

    private AstNode getPrimaryExpression(TokenizerWrapper wrapper) throws ParserException {
        if (next(wrapper, T_NULL)) {
            return new StringAstNode(null);
        } else if (next(wrapper, T_TRUE)) {
            return new BooleanAstNode(true);
        } else if (next(wrapper, T_FALSE)) {
            return new BooleanAstNode(false);
        } else if (next(wrapper, T_SLASH)) {
            return getRegexpLiteral(wrapper);
        } else if (next(wrapper, T_LITERAL_START)) {
            return getLiteralStatement(wrapper);
        } else if (test(wrapper, T_SQUOTE)) {
            return getStringLiteral(wrapper);
        } else if (test(wrapper, T_DQUOTE)) {
            return getStringLiteral(wrapper);
        } else if (next(wrapper, T_LBRACKET)) {
            return getArrayExpression(wrapper);
        } else if (next(wrapper, T_BLOCK_START)) {
            return getNodesStatement(wrapper, true);
        } else if (next(wrapper, T_THIS)) {
            return new ExpAstNode(AST_THIS);
        } else if (next(wrapper, T_GLOBAL)) {
            return new ExpAstNode(AST_GLOBAL);
        } else if (test(wrapper, T_INT)) {
            return new LongAstNode(Integer.parseInt(wrapper.next().first().getValue(), 10));
        } else if (test(wrapper, T_BIN)) {
            return new LongAstNode(Integer.parseInt(wrapper.next().first().getValue().substring(2), 2));
        } else if (test(wrapper, T_HEX)) {
            return new LongAstNode(Integer.parseInt(wrapper.next().first().getValue().substring(2), 16));
        } else if (test(wrapper, T_FLOAT)) {
            return new FloatAstNode(Float.parseFloat(wrapper.next().first().getValue()));
        } else if (test(wrapper, T_REF)) {
            return new ExpAstNode(AST_REF)
                    .add(new StringAstNode(wrapper.next().first().getValue()));
        } else if (next(wrapper, T_LPAREN)) {
            return getParenthesizedExpression(wrapper);
        } else {
            throw buildUnexpectedTokenException(wrapper, "EXPRESSION");
        }
    }

    private ExpAstNode getArrayExpression(TokenizerWrapper wrapper) throws ParserException {
        int counter = 0;

        Map<String, Object> values = new HashMap<>();

//        Token key, value;

        ExpAstNode result = new ExpAstNode(AST_ARRAY);
//        String key;
//        ExpAstNode value;
        AstNode key;
        AstNode value;
        Map<String, AstNode> map = new LinkedHashMap<>();


        do {
            while (next(wrapper, T_COMMA)) ;
            if (next(wrapper, T_RBRACKET)) {
                return result;
            }
            TokenizerResult tokenRes = wrapper.next(T_PROP.getId(), T_COLON.getId());
            if (tokenRes.isFound()) {
                map.put(tokenRes.firstValue(), getExpression(wrapper));
            } else {
                key = getExpression(wrapper);
                if (key.hasValue()) {
                    Object val = ((ValueNode) key).getValue();
                    if ((ParserUtils.isString(val) || ParserUtils.isNumber(val)) && next(wrapper, T_COLON)) {
                        value = getExpression(wrapper);
                        Object mapKey = val;
                        if (ParserUtils.isString(val) && ParserUtils.isInt((String) val)) {
                            mapKey = Integer.valueOf((String) val); //todo check this
                        }
                        Float f = ParserUtils.isFloat((String) val);
                        if (f != null) {
                            if (f.intValue() < counter) {
                                mapKey = f.intValue() + "";
                            } else {
                                counter = f.intValue();
                                mapKey = (counter++) + "";
                            }
                        }
                        map.put(mapKey + "", value);
                    } else {
                        map.put((counter++) + "", key);
                    }
                }
            }
        } while (next(wrapper, T_COMMA));

        if (!next(wrapper, T_RBRACKET)) {
            throw buildUnexpectedTokenException(wrapper, "]");
        }
        for (Map.Entry<String, AstNode> entry : map.entrySet()) {
            result.add(new StringAstNode(entry.getKey()))
                    .add(entry.getValue());
        }

        return result;
    }

    private boolean isStringOrNumber(AstNode node) {
        if (node instanceof ValueNode) {
            Object value = ((ValueNode) node).getValue();
            if (value instanceof String
                    || value instanceof Double
                    || value instanceof Long) {
                return true;
            }
        }
        return false;
    }

    private StringAstNode getStringLiteral(TokenizerWrapper wrapper) throws ParserException {
        String start = wrapper.next().first().getValue();
        TokenizerResult fragment;
        final StringBuilder builder = new StringBuilder();
        while ((fragment = wrapper.next()).isFound()) {
            if (fragment.first().getTypes().contains(T_EOF.getId())) {
                throw buildSyntaxErrorException(wrapper, "unterminated string literal");
            }
            if (StringUtils.equals(fragment.first().getValue(), start)) {
                break;
            } else if (StringUtils.equals(fragment.first().getValue(), "\\")) {
                builder.append("\\")
                        .append(wrapper.next().first().getValue());
            } else {
                builder.append(fragment.first().getValue());
            }
        }
        return new StringAstNode(builder.toString()).escaped();
    }

    private StringAstNode getLiteralStatement(TokenizerWrapper wrapper) throws ParserException {
        wrapper = new TokenizerWrapper(wrapper);
        final StringBuilder builder = new StringBuilder("");
        while (wrapper.test(T_EOF.getId(), T_LITERAL_END.getId()) == null) {
            builder.append(wrapper.next().first().getValue());
        }
        if (!next(wrapper, T_LITERAL_END)) {
            throw buildUnexpectedTokenException(wrapper, "%}}");
        }
        return new StringAstNode(builder.toString());
    }

    private AstNode getParenthesizedExpression(TokenizerWrapper wrapper) throws ParserException {
        AstNode node = getExpression(wrapper);
        if (!next(wrapper, T_RPAREN)) {
            throw buildUnexpectedTokenException(wrapper, ")");
        }
        return node;
    }

    private ExpAstNode getRegexpLiteral(TokenizerWrapper wrapper) throws ParserException {
        final StringBuilder result = new StringBuilder("");
        boolean inCharSet = false;

        for (; ; ) {
            if (wrapper.test(T_EOF.getId()).isFound()) {
                break;
            }
            if (test(wrapper, T_EOL)) {
                break;
            }
            if (!inCharSet && test(wrapper, T_SLASH)) {
                break;
            }
            if (next(wrapper, T_BACKSLASH)) {
                result.append("\\");
            } else if (test(wrapper, T_LBRACKET)) {
                inCharSet = true;
            } else if (test(wrapper, T_RBRACKET)) {
                inCharSet = false;
            }
            result.append(wrapper.next().first().getValue());
        }

        if (!next(wrapper, T_SLASH)) {
            throw buildSyntaxErrorException(wrapper, "unterminated regexp literal");
        }

        try {
            Pattern.compile(result.toString());// TODO why result is ignored
        } catch (Exception e) {
            throw buildSyntaxErrorException(wrapper, e.getMessage());
        }

        int flagNum = 0;
        Token flagToken = wrapper.next(T_PROP).first();
        if (flagToken != null) {
            String flagStr = flagToken.getValue();

            if (!regexpFlagsPattern.matcher(flagStr).find()) {
                final String msg = "invalid flags supplied to regular expression '" + flagStr + "'";
                throw buildSyntaxErrorException(wrapper, msg);
            }

            if (flagStr.contains("g")) {
                flagNum |= AstRegexType.RE_GLOBAL.getId();
            }
            if (flagStr.contains("m")) {
                flagNum |= AstRegexType.RE_MULTILINE.getId();
            }
            if (flagStr.contains("i")) {
                flagNum |= AstRegexType.RE_IGNORECASE.getId();
            }
        }

        return new ExpAstNode(AST_REGEXP)
                .add(new StringAstNode(result.toString()))
                .add(new LongAstNode(flagNum));
    }

    private boolean next(TokenizerWrapper wrapper, Tokens... tokens) {
        return wrapper.next(toIntArr(tokens)).isFound();
    }

    private boolean test(TokenizerWrapper wrapper, Tokens... tokens) {
        return wrapper.test(toIntArr(tokens)).isFound();
    }

    private Integer[] toIntArr(Tokens... tokens) {
        Integer[] ids = new Integer[tokens.length];
        for (Integer i = 0; i < tokens.length; i++) {
            ids[i] = tokens[i].getId();
        }
        return ids;
    }

    private ParserException buildUnexpectedTokenException(
            TokenizerWrapper wrapper, String expected
    ) throws ParserException {
        Token token = wrapper.next().first();
        int line = wrapper.getLineNumber(token.getIndex());
        String value = token.getTypes().contains(T_EOF.getId()) ? "EOF" : token.getValue();
        String message = "unexpected '" + value + "', expected '" + expected + "'";
        return new UnexpectedTokenException(message, wrapper.getBaseURI(), line);
    }

    private SyntaxErrorException buildSyntaxErrorException(TokenizerWrapper wrapper, String s) throws ParserException {
        Token token = wrapper.next().first();
        int line = wrapper.getLineNumber(token.getIndex());
        return new SyntaxErrorException(s, wrapper.getBaseURI(), line);
    }
}
