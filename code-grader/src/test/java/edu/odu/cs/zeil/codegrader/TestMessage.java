package edu.odu.cs.zeil.codegrader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import org.junit.jupiter.api.Test;

public class TestMessage {

    @Test
    void testSimpleMessage() {
        String str = "Hello world!";
        Message msg = new Message(str);
        assertThat(msg.toString(), is(str));
        assertThat(msg.toHTML(), is(str));
    }

    @Test
    void testLongMessage() {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < 1000; ++i) {
            buf.append("abcdefghijklmnopqrstuvwxyz");
        }
        String str = buf.toString();
        Message msg = new Message(str);
        assertThat(msg.toString(), containsString("abcdef"));
        assertThat(msg.toString(), containsString("clipped"));
        assertThat(msg.toHTML(), containsString("abcdef"));
        assertThat(msg.toHTML(), containsString("clipped"));
    }

    @Test
    void testHTMLEncoding() {
        String input = "if (x < y && y > z)\n    quit";
        
        Message msg = new Message(input);
        assertThat(msg.toString(), is(input));
        assertThat(msg.toHTML(), containsString("x &lt; y"));
        assertThat(msg.toHTML(), containsString("y &gt; z"));
        assertThat(msg.toHTML(), containsString("y &amp;&amp; y"));
        assertThat(msg.toHTML(), containsString("z)<br/>"));
    }

    @Test
    void testHTMLPassThrough() {
        String input = "This is " + Message.HTML_PASSTHROUGH + "<i>"
            + Message.HTML_PASSTHROUGH + "italic" + Message.HTML_PASSTHROUGH
            + "</i>" + Message.HTML_PASSTHROUGH + " text.";
        
        Message msg = new Message(input);
        assertThat(msg.toString(), is("This is italic text."));
        assertThat(msg.toHTML(), is("This is <i>italic</i> text."));
    }

    @Test
    void testLongMessageWithPassThru() {
        StringBuffer buf = new StringBuffer();
        buf.append(Message.HTML_PASSTHROUGH);
        for (int i = 0; i < 1000; ++i) {
            buf.append("abcdefghijklmnopqrstuvwxyz");
        }
        buf.append(Message.HTML_PASSTHROUGH);
        String str = buf.toString();
        Message msg = new Message(str);
        assertThat(msg.toHTML(), containsString("abcdef"));
        assertThat(msg.toHTML(), not(containsString("clipped")));
    }


}
