package edu.odu.cs.zeil.codegrader;

/**
 * Messages to be displayed to the student within a grade report.
 */
public class Message {

	/**
	 * Alternately begin and ends a section fo text that should be
	 * omitted from plain-text messaging and passed unchanged into
	 * HTML messages.
	 */
	public static final String HTML_PASSTHROUGH = "\b";

	/**
	 * Used to indicate the end of captured output when std err is not also
	 * being captured.
	 */
	public static final String END_OF_OUTPUT_MARKER
		= "\n--- end of output ---\n";
	public static final String END_OF_OUTPUT_LINE
		= "--- end of output ---";

	/**
	 * Used to indicate the end of output amd beginning of captured
	 * error messages.
	 */
	public static final String OUTPUT_ERROR_MARKER 
		= END_OF_OUTPUT_MARKER + "--- std error ---\n";

	/**
	 * Used to indicate the end of captured std error messages.
	 */
	public static final String END_OF_ERROR_MARKER 
		= "\n--- end of std error ---\n";

	/**
	 * All messages are clipped to this length in reports.
	 */
	public static final int MAX_MESSAGE_LENGTH = 5000;

	/**
	 * The raw text of the message.
	 */
	private String msgText;

	/**
	 * Create a new message.
	 * 
	 * @param msg
	 */
	public Message(String msg) {
		if (msg.length() > MAX_MESSAGE_LENGTH) {
			msgText = msg.substring(0, MAX_MESSAGE_LENGTH - 1)
					+ "\n[clipped after "
					+ MAX_MESSAGE_LENGTH + " characters]";
		} else {
			msgText = msg;
		}
	}

	/**
	 * Render a message for inclusion in an HTML report.
	 * 
	 * @return message text suitable for pasting into an HTML file
	 */
	public String toHTML() {
				StringBuffer buf = new StringBuffer();
		boolean passingThrough = false;
		int i = 0;
		while (i < msgText.length()) {
			if (msgText.regionMatches(i, HTML_PASSTHROUGH,
					0, HTML_PASSTHROUGH.length())) {
				passingThrough = !passingThrough;
				i += HTML_PASSTHROUGH.length();
			} else {
				char c = msgText.charAt(i);
				if (passingThrough) {
					buf.append(c);
				} else if (c == '&') {
					buf.append("&amp;");
				} else if (c == '<') {
					buf.append("&lt;");
				} else if (c == '>') {
					buf.append("&gt;");
				} else if (c == '\n') {
					buf.append("<br/>");
				} else {
					buf.append(c);
				}
				++i;
			}
		}
		return buf.toString();
	}

	/**
	 * Render a message for inclusion in an HTML report.
	 * 
	 * @return message text suitable for pasting into an HTML file
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer();
		boolean passingThrough = false;
		int i = 0;
		while (i < msgText.length()) {
			if (msgText.regionMatches(i, HTML_PASSTHROUGH,
					0, HTML_PASSTHROUGH.length())) {
				passingThrough = !passingThrough;
				i += HTML_PASSTHROUGH.length();
			} else {
				char c = msgText.charAt(i);
				if (!passingThrough) {
					buf.append(c);
				}
				++i;
			}
		}
		return buf.toString();
	}

}
