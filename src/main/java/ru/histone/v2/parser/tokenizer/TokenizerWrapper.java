package ru.histone.v2.parser.tokenizer;

import ru.histone.tokenizer.Tokens;

import java.util.Collections;
import java.util.List;

/**
 * Created by inv3r on 15/01/16.
 */
public class TokenizerWrapper {
    private Tokenizer tokenizer;
    private List<Integer> ignored;

    public TokenizerWrapper(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.ignored = Collections.emptyList();
    }

    public TokenizerWrapper(TokenizerWrapper wrapper) {
        this(wrapper, Collections.emptyList());
    }

    public TokenizerWrapper(TokenizerWrapper wrapper, List<Integer> ignored) {
        this.tokenizer = wrapper.tokenizer;
        this.ignored = ignored;
    }

    public String getBaseURI() {
        return tokenizer.getBaseURI();
    }

    public TokenizerResult next(Integer... selector) {
        return tokenizer.next(ignored, selector);
    }

    public TokenizerResult next(Tokens token) {
        return next(token.getId());
    }

    public TokenizerResult test(Integer... selector) {
        return tokenizer.test(ignored, selector);
    }

    public int getLineNumber(long index) {
        return tokenizer.getLineNumber(index);
    }
}
