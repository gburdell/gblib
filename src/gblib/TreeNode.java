/*
 * The MIT License
 *
 * Copyright 2017 gburdell.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package gblib;

import java.util.LinkedList;

/**
 *
 * @author gburdell
 */
public class TreeNode<T> extends LinkedList<T> {

    public TreeNode() {
        this(null, null);
    }

    public TreeNode(T data) {
        this(data, null);
    }

    public TreeNode(TreeNode parent) {
        this(null, parent);
    }

    public TreeNode(T data, TreeNode parent) {
        m_data = data;
        m_parent = parent;
    }

    public TreeNode setParent(TreeNode parent) {
        m_parent = parent;
        return this;
    }

    public TreeNode getParent() {
        return m_parent;
    }

    public T getData() {
        return m_data;
    }
    
    public TreeNode setData(T data) {
        m_data = data;
        return this;
    }

    private T   m_data;
    private TreeNode m_parent;
}
