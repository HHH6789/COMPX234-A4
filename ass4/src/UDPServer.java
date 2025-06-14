// Import necessary Java libraries
// 导入必要的Java库
import java.io.File;
import java.io.RandomAccessFile;
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

                        // Create a UDP packet containing the error message, targeting the client's address and port
                        // 创建一个包含错误消息的UDP数据包，目标为客户端地址和端口
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);

                        // Send the error message packet back to the client
                        // 将错误消息数据包发送回客户端
                        socket.send(sendPacket);

                        continue;
                    }

                    // Start new thread for file transfer
                    // 启动新线程处理文件传输
                    new Thread(() -> handleFileTransfer(file, clientAddress, clientPort)).start();
                    
                }

            }

        }
        // Catch any exceptions that might occur during server operation
        // 捕获服务器运行期间可能发生的任何异常
        catch (Exception e) {
            // Print the error message to standard output
            // 将错误信息打印到标准输出
            System.out.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }

    }

    // Method to handle file transfer with client
    // 处理与客户端的文件传输的方法
    private static void handleFileTransfer(File file, InetAddress clientAddress, int clientPort){
        try {
            // Select random port for data transfer
            // 为数据传输选择随机端口
            int dataPort = MIN_DATA_PORT + random.nextInt(MAX_DATA_PORT - MIN_DATA_PORT + 1);

            // Create socket for data transfer
            // 创建用于数据传输的套接字
            DatagramSocket dataSocket = new DatagramSocket(dataPort);

            // Format the success response message with file details and data port
            // 格式化成功响应消息，包含文件详情和数据端口
            String okMsg = String.format("OK %s SIZE %d PORT %d", file.getName(), file.length(), dataPort);

            // Convert the message string to bytes for network transmission
            // 将消息字符串转换为字节数组用于网络传输
            byte[] sendData = okMsg.getBytes();

            // Create a UDP packet containing the response message, targeting client's address and port
            // 创建包含响应消息的UDP数据包，目标为客户端地址和端口
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);

            // Send the response packet to the client through the data socket
            // 通过数据套接字将响应数据包发送给客户端
            dataSocket.send(sendPacket);

            // Open file for reading
            // 打开文件进行读取
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")){
                // Buffer for incoming requests
                // 接收请求的缓冲区
                byte[] receiveData = new byte[1024];


                // File transfer loop
                // 文件传输循环
                while (true){
                    // Create a DatagramPacket to store incoming data with the receive buffer
                    // 创建一个DatagramPacket对象，用于存储接收到的数据，使用接收缓冲区
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                    // Block and wait to receive a UDP packet from the network
                    // 阻塞并等待从网络接收UDP数据包
                    dataSocket.receive(receivePacket);

                    // Convert the received packet data bytes into a string and trim whitespace
                    // 将接收到的数据包字节数据转换为字符串并去除首尾空格
                    String request = new String(receivePacket.getData(), 0, receivePacket.getLength()).trim();

                    // Split the request string into parts using space as delimiter
                    // 使用空格作为分隔符将请求字符串分割成多个部分
                    String[] parts = request.split(" ");

                    // Check if the first part is "FILE" command and has at least 2 elements :command and filename
                    // 检查第一部分是否为"FILE"命令且至少包含2个元素:命令和文件名
                    if (parts[0].equals("FILE") && parts.length >= 2){
                        String filename = parts[1];

                        // Handle close request
                        // 处理关闭请求
                        if (parts[2].equals("CLOSE")){
                            // Prepare the close acknowledgement message with filename
                            // 准备包含文件名的关闭确认消息
                            String closeOkMsg = "FILE " + filename + " CLOSE_OK";

                            // Convert the message string to bytes for network transmission
                            // 将消息字符串转换为字节数组用于网络传输
                            byte[] closeOkData = closeOkMsg.getBytes();

                            // Create UDP packet with the close acknowledgement, targeting client's address and port
                            // 创建包含关闭确认的UDP数据包，目标为客户端地址和端口
                            DatagramPacket closeOkPacket = new DatagramPacket(closeOkData, closeOkData.length, clientAddress, clientPort);

                            // Send the close acknowledgement packet to the client
                            // 将关闭确认数据包发送给客户端
                            dataSocket.send(closeOkPacket);
                            break;


                        }

                        // Handle data request
                        // 处理数据请求
                        else if (parts[2].equals("GET") && parts.length >= 7){
                            // Parse the starting byte position from request parts ,converting String to long
                            // 从请求部分解析起始字节位置
                            long start = Long.parseLong(parts[4]);

                            // Parse the ending byte position from request parts ,converting String to long
                            // 从请求部分解析结束字节位置
                            long end = Long.parseLong(parts[6]);

                            // Calculate the block size needed ,positions are inclusive
                            // 计算需要的块大小,起止位置是包含
                            int blockSize = (int) (end - start + 1);

                        }



                    }


                }


            }

        } catch (Exception e) {
        }
    }







}
