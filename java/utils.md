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
				sb.append(line).append("\r\n");
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

    private static List<String> partitionCommandLine(final String command) {
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

## IP地址与long型的转换

```
public class IPUtil {  
    /** 
     * ip地址转成long型数字 
     * 将IP地址转化成整数的方法如下： 
     * 1、通过String的split方法按.分隔得到4个长度的数组 
     * 2、通过左移位操作（<<）给每一段的数字加权，第一段的权为2的24次方，第二段的权为2的16次方，第三段的权为2的8次方，最后一段的权为1 
     * @param strIp 
     * @return 
     */  
    public static long ipToLong(String strIp) {  
        String[]ip = strIp.split("\\.");  
        return (Long.parseLong(ip[0]) << 24) + (Long.parseLong(ip[1]) << 16) + (Long.parseLong(ip[2]) << 8) + Long.parseLong(ip[3]);  
    }  
  
    /** 
     * 将十进制整数形式转换成127.0.0.1形式的ip地址 
     * 将整数形式的IP地址转化成字符串的方法如下： 
     * 1、将整数值进行右移位操作（>>>），右移24位，右移时高位补0，得到的数字即为第一段IP。 
     * 2、通过与操作符（&）将整数值的高8位设为0，再右移16位，得到的数字即为第二段IP。 
     * 3、通过与操作符吧整数值的高16位设为0，再右移8位，得到的数字即为第三段IP。 
     * 4、通过与操作符吧整数值的高24位设为0，得到的数字即为第四段IP。 
     * @param longIp 
     * @return 
     */  
    public static String longToIP(long longIp) {  
        StringBuffer sb = new StringBuffer("");  
        // 直接右移24位  
        sb.append(String.valueOf((longIp >>> 24)));  
        sb.append(".");  
        // 将高8位置0，然后右移16位  
        sb.append(String.valueOf((longIp & 0x00FFFFFF) >>> 16));  
        sb.append(".");  
        // 将高16位置0，然后右移8位  
        sb.append(String.valueOf((longIp & 0x0000FFFF) >>> 8));  
        sb.append(".");  
        // 将高24位置0  
        sb.append(String.valueOf((longIp & 0x000000FF)));  
        return sb.toString();  
    }  
  
}
```