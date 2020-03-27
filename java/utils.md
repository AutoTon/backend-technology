# 工具类

## JAVA执行Linux命令

```
public class ProcessUtils {
	private static final String CMD_OUTPUT_CHARSET_UTF_8 = "UTF-8";
	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessUtils.class);

	private ProcessUtils() {
	}

	/**
	 * 执行指定命令
	 * 
	 * @param command
	 *            命令列表,包含命令和它的参数
	 * @return
	 */
	public static ProcessResult exec(List<String> command) {
		LOGGER.info("Execute command: {}", command);
		ProcessBuilder pb = new ProcessBuilder(command);
		pb.redirectErrorStream(true);
		try {
			Process process = pb.start();
			BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream(),
					Charset.forName(CMD_OUTPUT_CHARSET_UTF_8)));
			String line = null;
			StringBuilder sb = new StringBuilder();
			while ((line = br.readLine()) != null) {
				LOGGER.debug(line);
				sb.append(line + "\r\n");
			}
			int exitValue = process.waitFor();
			ProcessResult result = new ProcessResult();
			LOGGER.info("Return value {} from command: {}", exitValue, command);
			result.setExitValue(exitValue);
			result.setSuccess(exitValue == 0);
			result.setOutput(sb.toString());
			return result;
		} catch (IOException | InterruptedException e) {
			String msg = String.format("Error when executing command [%s]", command);
			throw new RuntimeException(msg, e);
		}
	}

    public static List<String> partitionCommandLine(final String command) {
        final List<String> commands = new ArrayList<>(16);

        int index = 0;

        StringBuffer buffer = new StringBuffer(command.length());

        boolean isApos = false;
        boolean isQuote = false;
        while (index < command.length()) {
            final char c = command.charAt(index);

            switch (c) {
                case ' ':
                    if (!isQuote && !isApos) {
                        final String arg = buffer.toString();
                        buffer = new StringBuffer(command.length() - index);
                        if (arg.length() > 0) {
                            commands.add(arg);
                        }
                    } else {
                        buffer.append(c);
                    }
                    break;
                case '\'':
                    if (!isQuote) {
                        isApos = !isApos;
                    } else {
                        buffer.append(c);
                    }
                    break;
                case '"':
                    if (!isApos) {
                        isQuote = !isQuote;
                    } else {
                        buffer.append(c);
                    }
                    break;
                default:
                    buffer.append(c);
            }

            index++;
        }

        if (buffer.length() > 0) {
            final String arg = buffer.toString();
            commands.add(arg);
        }
        return commands;
    }

	public static ProcessResult exec(String command) {
        List<String> commands = partitionCommandLine(command);
        return exec(commands);
    }

	public static class ProcessResult {
		private boolean isSuccess;
		private int exitValue;
		private String output;

		public boolean isSuccess() {
			return isSuccess;
		}

		public void setSuccess(boolean isSuccess) {
			this.isSuccess = isSuccess;
		}

		public String getOutput() {
			return output;
		}

		public void setOutput(String output) {
			this.output = output;
		}

		public int getExitValue() {
			return exitValue;
		}

		public void setExitValue(int exitValue) {
			this.exitValue = exitValue;
		}

	}
}
```

## 生成随机字符串

```
public class RandomUtil {

    /**
     * 生成随机字符串
     *
     * @param length 生成的字符串长度
     * @return 随机字符串
     */
    public static String generate(int length) {
        String string = "0123456789";
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) Math.floor(Math.random() * string.length());
            stringBuilder.append(string.charAt(index));
        }
        return stringBuilder.toString();
    }

}
```