// Import necessary Java libraries
// 导入必要的Java库
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
    public static void main(String[] args)throws IOException{
        // Create a UDP socket bound to port 51234
        // 创建一个绑定到51234端口的UDP套接字
        DatagramSocket socket = new DatagramSocket(51234);
        // Print server startup message
        // 打印服务器启动信息
        System.out.println("Server started on port 51234");

        // Create a buffer to store incoming data
        // 创建一个缓冲区来存储接收到的数据
        byte[] buffer = new byte[1024];


        // Main server loop to continuously handle client requests
        // 服务器主循环，持续处理客户端请求
        while (true) {
            // Print waiting message
            // 打印等待消息
            System.out.println("Waiting for client data...");
            // Create a packet to receive data
            // 创建一个数据包来接收数据
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            // Block until a packet is received
            // 阻塞直到接收到数据包
            socket.receive(packet);

            // Convert received bytes to UTF-8 string
            // 将接收到的字节转换为UTF-8字符串
            String received = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
            // Print received message with client info
            // 打印接收到的消息及客户端信息
            System.out.println("Received from client (" + packet.getAddress() + ":" + packet.getPort() + "): " + received);

            // 假设服务器只是把接收到的内容原样返回
            String response = "Server received: " + received;
            // Convert response string to bytes using UTF-8
            // 将响应字符串转换为UTF-8字节数组
            byte[] responseData = response.getBytes(StandardCharsets.UTF_8);
            
            // Create response packet with client address and port
            // 创建响应数据包（包含客户端地址和端口）
            DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length,packet.getAddress(), packet.getPort());

            // Send the response back to client
            // 将响应发送回客户端
            socket.send(responsePacket);

            // Print sent response for debugging
            // 打印已发送的响应（用于调试）
            System.out.println("Response sent: " + response);
        }

    }

    // Method to handle file transfer with client
    // 处理与客户端的文件传输的方法
    private static void handleFileTransfer(File file, InetAddress clientAddress, int clientPort){
        // 获取当前线程ID
        long threadId = Thread.currentThread().getId();
    
        // 添加线程开始日志
        System.out.printf("[%tT] [线程-%d] 开始处理文件 %s (客户端: %s:%d)%n",
        System.currentTimeMillis(), threadId, file.getName(), clientAddress, clientPort);

        synchronized (System.out) {
        System.out.printf("[%tT] [线程-%d] 开始处理文件 %s (客户端: %s:%d)%n",System.currentTimeMillis(), threadId, file.getName(), clientAddress, clientPort);
        System.out.flush();
        }

        try {

            
            // Select random port for data transfer
            // 为数据传输选择随机端口
            int dataPort = MIN_DATA_PORT + random.nextInt(MAX_DATA_PORT - MIN_DATA_PORT + 1);

             // 添加端口分配日志
            System.out.printf("[%tT] [线程-%d] 为文件 %s 分配端口: %d%n",System.currentTimeMillis(), threadId, file.getName(), dataPort);

            //Add port allocation log
            //添加端口分配日志
            
            System.out.println("[Server] Thread for file " + file.getName() + " using data port: " + dataPort + " (Client: " + clientAddress + ":" + clientPort + ")");


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

                            // Create a byte array buffer of the calculated block size
                            // 创建计算得出的块大小的字节数组缓冲区
                            byte[] fileData = new byte[blockSize];

                            // Move file pointer to the specified starting position
                            // 将文件指针移动到指定的起始位置
                            raf.seek(start);

                            // Read the requested block of data from file into the buffer
                            // 从文件中读取请求的数据块到缓冲区
                            int bytesRead = raf.read(fileData);


                            // If data was read successfully
                            // 如果成功读取数据
                            if (bytesRead > 0){
                                // Encode data in Base64
                                // 将数据编码为Base64格式
                                String base64Data = Base64.getEncoder().encodeToString(bytesRead == blockSize ? fileData : java.util.Arrays.copyOf(fileData, bytesRead));

                                // Prepare response with file data
                                // 准备包含文件数据的响应
                                String response = String.format("FILE %s OK START %d END %d DATA %s", filename, start, start + bytesRead - 1, base64Data);

                                // Convert the response string to a byte array for network transmission
                                // 将响应字符串转换为字节数组以便网络传输
                                byte[] responseData = response.getBytes();

                                // Create a UDP packet containing the response data with client address and port
                                // 创建包含响应数据的UDP数据包，指定客户端地址和端口
                                DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, clientAddress, clientPort);

                                // Send the response packet back to the client through the data socket
                                // 通过数据套接字将响应数据包发送回客户端
                                dataSocket.send(responsePacket);

                            }

                          
                        }



                    }


                }


            }
            // Close data socket when done
            // 完成后关闭数据套接字
            dataSocket.close();

        } catch (Exception e) {
            // +++ 改进：异常日志同步输出 +++
            synchronized (System.err) {
            System.err.printf("[%tT] [线程-%d] 处理文件 %s 时出错: %s%n",System.currentTimeMillis(), threadId, file.getName(), e.getMessage());
            e.printStackTrace();
            System.err.flush();
            }
        }finally {
            // +++ 确保线程结束日志输出 +++
            synchronized (System.out) {
                System.out.printf("[%tT] [线程-%d] 文件 %s 传输完成%n",System.currentTimeMillis(), threadId, file.getName());
                System.out.flush();
            }
        }
    }







}
