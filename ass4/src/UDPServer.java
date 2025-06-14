// Import necessary Java libraries
// 导入必要的Java库
import java.net.DatagramSocket;
import java.util.Random;

// Main server class for UDP file transfers
// UDP文件传输的主服务器类
public class UDPServer {
    // Minimum port number for data transfer
    // 数据传输的最小端口号
    private static final int MIN_DATA_PORT = 50000;
    // Maximum port number for data transfer
    // 数据传输的最大端口号
    private static final int MAX_DATA_PORT = 51000;

    // Maximum block size for file transfer
    // 文件传输的最大块大小
    private static final int MAX_BLOCK_SIZE = 1000;

    // Random number generator for port selection
    // 用于端口选择的随机数生成器
    private static final Random random = new Random();

     // Main method to start the server
    // 启动服务器的主方法
    public static void main(String[] args){
        // Check for correct command line arguments
        // 检查命令行参数是否正确
        if (args.length != 1) {
            //output "Usage: java UDPServer <port>"
            //输出
            System.out.println("Usage: java UDPServer <port>");
            return;


        }
        // Parse port number from arguments
        // 从参数中解析端口号
        int port = Integer.parseInt(args[0]);

        // Create UDP socket and handle exceptions
        // 创建UDP套接字并处理异常
        try (DatagramSocket socket = new DatagramSocket(port)){
            //打印服务器启动信息，显示监听的端口号
            // Print server startup information, displaying the port being listened on
            System.out.println("Server started on port " + port);

        }

    }

}
