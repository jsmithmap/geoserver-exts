package org.geotools.ysld.encode;

import org.geotools.filter.text.ecql.ECQL;
import org.geotools.ysld.Tuple;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Encoder<T> implements Iterator<Object> {

    Deque<Map<String,Object>> stack = new ArrayDeque<Map<String, Object>>();

    public Encoder() {
        reset();
    }

    Iterator<T> it;

    Encoder(Iterator<T> it) {
        this.it = it;
    }

    Encoder(T obj) {
        this.it = obj != null ? Collections.singleton(obj).iterator() : (Iterator) Collections.emptyIterator();
    }

    @Override
    public boolean hasNext() {
        return it.hasNext();
    }

    @Override
    public Object next() {
        reset();
        encode(it.next());
        return root();
    }

    protected abstract void encode(T next);

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    Encoder reset() {
        stack.clear();;
        stack.push(newMap());
        return this;
    }

    Encoder push(String key) {
        Map<String,Object> map = newMap();
        stack.peek().put(key, map);
        stack.push(map);
        return this;
    }

    Encoder pop() {
        stack.pop();
        return this;
    }

    Encoder put(String key, Object val) {
        if (val != null) {
            stack.peek().put(key, val);
        }
        return this;
    }

    Encoder put(String key, Expression expr) {
        if (expr != null) {
            put(key, toObjOrNull(expr));
        }
        return this;
    }

    Encoder put(String key, Expression e1, Expression e2) {
        Tuple t = Tuple.of(toObjOrNull(e1), toObjOrNull(e2));
        if (!t.isNull()) {
            put(key, t.toString());
        }
        return this;
    }

    Encoder inline(Encoder<?> e) {
        if (e.hasNext()) {
            e.next();
            inline(e.root());
        }
        return this;
    }

    Encoder inline(Map<String,Object> values) {
        stack.peek().putAll(values);
        return this;
    }

    Object toObjOrNull(Expression expr) {
        String str = expr != null ? ECQL.toCQL(expr) : null;
        if (str != null) {
            // strip quotes
            if (str.charAt(0) == '\'') {
                str = str.substring(1);
            }
            if (str.charAt(str.length()-1) == '\'') {
                str = str.substring(0, str.length()-1);
            }

        }

        if (str != null) {
            try {
                return Long.parseLong(str);
            }
            catch(NumberFormatException e1) {
                try {
                    return Double.parseDouble(str);
                }
                catch(NumberFormatException e2) {
                    if ("true".equalsIgnoreCase(str) || "false".equalsIgnoreCase(str)) {
                        return Boolean.parseBoolean(str);
                    }
                }
            }
        }

        return str;
    }

    Expression nullIf(Expression expr, double value) {
        return nullIf(expr, value, Double.class);
    }

    Expression nullIf(Expression expr, String value) {
        return nullIf(expr, value, String.class);
    }

    <T> Expression nullIf(Expression expr, T value, Class<T> clazz) {
        if (expr instanceof Literal) {
            T t = expr.evaluate(null, clazz);
            if (t != null && t.equals(value)) {
                return null;
            }
        }
        return expr;
    }

    Map<String,Object> get() {
        return stack.peek();
    }

    Map<String,Object> root() {
        return stack.getLast();
    }

    Map<String,Object> newMap() {
        return new LinkedHashMap<String, Object>();
    }
}
