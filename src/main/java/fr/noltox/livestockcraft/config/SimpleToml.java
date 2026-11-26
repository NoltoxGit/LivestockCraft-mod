package fr.noltox.livestockcraft.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Reader for the small TOML subset used by the mod's config file, so no TOML library has to be bundled.
 * <p>
 * Supported: {@code #} comments, {@code [table]} headers, bare {@code key = value} pairs, and values that are
 * booleans, integers ({@link Long}), floats ({@link Double}) or single-line arrays of those.
 * Keys are returned flattened as {@code table.key}. Anything else is rejected with a {@link ParseException}.
 */
public final class SimpleToml {

	private static final Pattern NAME = Pattern.compile("[A-Za-z0-9_-]+");
	private static final Pattern INTEGER = Pattern.compile("[+-]?\\d+");
	private static final Pattern FLOAT = Pattern.compile("[+-]?\\d+(\\.\\d+)?([eE][+-]?\\d+)?");

	private SimpleToml() {
	}

	public static final class ParseException extends Exception {
		public ParseException(int line, String message) {
			super("line " + line + ": " + message);
		}
	}

	public static Map<String, Object> parse(String text) throws ParseException {
		Map<String, Object> values = new LinkedHashMap<>();
		String table = "";
		String[] lines = text.split("\\R", -1);
		for (int i = 0; i < lines.length; i++) {
			int lineNumber = i + 1;
			String line = stripComment(lines[i]).strip();
			if (line.isEmpty()) {
				continue;
			}
			if (line.startsWith("[")) {
				String name = line.endsWith("]") ? line.substring(1, line.length() - 1).strip() : "";
				if (!NAME.matcher(name).matches()) {
					throw new ParseException(lineNumber, "invalid table header: " + line);
				}
				table = name + ".";
				continue;
			}
			int equals = line.indexOf('=');
			if (equals < 0) {
				throw new ParseException(lineNumber, "expected 'key = value': " + line);
			}
			String key = line.substring(0, equals).strip();
			if (!NAME.matcher(key).matches()) {
				throw new ParseException(lineNumber, "invalid key: " + key);
			}
			String fullKey = table + key;
			if (values.containsKey(fullKey)) {
				throw new ParseException(lineNumber, "duplicate key: " + fullKey);
			}
			values.put(fullKey, parseValue(line.substring(equals + 1).strip(), lineNumber));
		}
		return values;
	}

	private static String stripComment(String line) {
		int hash = line.indexOf('#');
		return hash < 0 ? line : line.substring(0, hash);
	}

	private static Object parseValue(String raw, int lineNumber) throws ParseException {
		if (!raw.startsWith("[")) {
			return parseScalar(raw, lineNumber);
		}
		if (!raw.endsWith("]")) {
			throw new ParseException(lineNumber, "arrays must fit on one line: " + raw);
		}
		String body = raw.substring(1, raw.length() - 1).strip();
		List<Object> items = new ArrayList<>();
		if (body.isEmpty()) {
			return List.of();
		}
		if (body.endsWith(",")) {
			body = body.substring(0, body.length() - 1);
		}
		for (String item : body.split(",", -1)) {
			items.add(parseScalar(item.strip(), lineNumber));
		}
		return List.copyOf(items);
	}

	private static Object parseScalar(String raw, int lineNumber) throws ParseException {
		if (raw.isEmpty()) {
			throw new ParseException(lineNumber, "missing value");
		}
		if (raw.equals("true") || raw.equals("false")) {
			return Boolean.parseBoolean(raw);
		}
		try {
			if (INTEGER.matcher(raw).matches()) {
				return Long.parseLong(raw);
			}
			if (FLOAT.matcher(raw).matches()) {
				return Double.parseDouble(raw);
			}
		} catch (NumberFormatException e) {
			throw new ParseException(lineNumber, "number out of range: " + raw);
		}
		throw new ParseException(lineNumber, "unsupported value: " + raw);
	}
}
