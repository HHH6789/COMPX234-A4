// Import necessary Java libraries
// 导入必要的Java库
import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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

            // Buffer for incoming data
            // 接收数据的缓冲区
            byte[] receiveData = new byte[1024];


            // Main server loop
            // 服务器主循环
            while (true){
                // Prepare packet for receiving data
                // 准备接收数据的包
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                // Wait for incoming packet
                // 等待传入的数据包
                socket.receive(receivePacket);

                // Convert received data to string
                // 将接收到的数据转换为字符串
                String request = new String(receivePacket.getData(), 0, receivePacket.getLength()).trim();


                // Split request into parts
                // 将请求拆分为多个部分
                String[] parts = request.split(" ");

                 // Validate request format
                // 验证请求格式
                if (parts.length < 2){
                    // Invalid message
                    //无效消息
                    continue;

                }

                // Handle DOWNLOAD request
                // 处理DOWNLOAD请求
                if (parts[0].equals("DOWNLOAD")){
                    // Extract the filename from the request parts
                    // 从请求部分中提取文件名
                    String filename = parts[1];

                    // Create a File object representing the requested file
                    // 创建一个表示请求文件的File对象
                    File file = new File(filename);

                    // Get the client's IP address from the received packet
                    // 从接收到的数据包中获取客户端的IP地址
                    InetAddress clientAddress = receivePacket.getAddress();


                    // Check if file exists
                    // 检查文件是否存在
                    if (!file.exists() || !file.isFile()){
                        // Send error message if file not found
                        // 如果文件不存在则发送错误消息
                        String errorMsg = "ERR " + filename + " NOT_FOUND";

                        // Convert the error message string to a byte array for network transmission
                        // 将错误消息字符串转换为字节数组以便网络传输
                        byte[] sendData = errorMsg.getBytes();
                    }

                    // Start new thread for file transfer
                    // 启动新线程处理文件传输
                    new Thread(() -> handleFileTransfer(file, clientAddress, clientPort)).start();
                    
                }

            }

        }

    }

}
