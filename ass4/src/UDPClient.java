import java.io.*;
import java.net.*;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;

//UDPClient class
//UDP客户端类
public class UDPClient{
    // Maximum number of retries for sending/receiving packets
    // 发送/接收数据包的最大重试次数
    private static final int MAX_RETRIES = 5;

    // Initial timeout value in milliseconds for socket operations
    // 套接字操作的初始超时时间（毫秒）
    private static final int INITIAL_TIMEOUT = 1000; 

    // Maximum size of each file block to be transferred
    // 每个文件块传输的最大大小
    private static final int MAX_BLOCK_SIZE = 1000;

    // Main method to start the UDP client
    // 主方法，启动UDP客户端
    public static void main(String[] args){
        // Check if correct number of arguments are provided
        // 检查参数数量是否正确
        if (args.length != 3) {
            //print message
            //输出打印相关信息
            System.out.println("Usage: java UDPClient <hostname> <port> <filelist>");
            return;


        }
        // Get the server hostname/IP address from the first command line argument
        // 从第一个命令行参数获取服务器的主机名/IP地址
        String hostname = args[0];

        // Get the server port number from the second command line argument and convert it to integer
        // 从第二个命令行参数获取服务器端口号并转换为整数
        int port = Integer.parseInt(args[1]);

        
        // Get the filename containing the list of files to download from the third command line argument
        // 从第三个命令行参数获取包含要下载文件列表的文件名
        String fileListName = args[2];

        // Create DatagramSocket and BufferedReader for file list
        // 创建DatagramSocket和用于读取文件列表的BufferedReader
        try (DatagramSocket socket = new DatagramSocket();BufferedReader fileListReader = new BufferedReader(new FileReader(fileListName))){
            // Get server address from hostname
            // 根据主机名获取服务器地址
            InetAddress serverAddress = InetAddress.getByName(hostname);
            String filename;
            
            // Read each filename from the file list
            // 从文件列表中逐行读取文件名
            while ((filename = fileListReader.readLine()) != null){
                // Trim any leading or trailing whitespace from the filename read from the file list
                // 去除从文件列表中读取的文件名首尾的空白字符
                filename = filename.trim();


                // Skip this iteration if the filename is empty after trimming (blank line in file list)
                // 如果文件名在去除空白后为空（文件列表中的空行），则跳过当前循环
                if (filename.isEmpty()) continue;

                // Print the filename that is being requested from the server
                // 打印正在向服务器请求的文件名
                System.out.println("Requesting file: " + filename);

                // Construct the DOWNLOAD request message by concatenating the command with the filename
                // 构造DOWNLOAD请求消息，将命令与文件名连接起来
                String downloadMsg = "DOWNLOAD " + filename;

                // Send the DOWNLOAD request to the server and wait for the response
                //向服务器发送DOWNLOAD请求并等待响应
                String response = sendAndReceive(socket, serverAddress, port, downloadMsg);

                // Check if the response is null
                // 检查响应是否为null
                if (response == null){
                    // Print error message when failed to get response after retries
                    // 当重试后仍无法获取响应时打印错误信息
                    System.out.println("Failed to get response for DOWNLOAD request after retries");

                    // Skip to next iteration in the loop
                    // 跳过本次循环继续下一次迭代
                    continue;
                }

                // Split response into parts
                // 将响应拆分为多个部分
                String[] parts = response.split(" ");

                // Check if the first part of the response indicates an error ("ERR")
                // 检查响应第一部分是否为错误标识 ("ERR")
                if (parts[0].equals("ERR")){
                    // Print the server error message with the full response
                    // 打印服务器错误信息（包含完整响应）
                    System.out.println("Server error: " + response);
                    continue;

                }

                // Validate response starts with OK
                // 验证响应是否以OK开头
                if (!parts[0].equals("OK")){
                    // Print an error message indicating an invalid response from the server  
                    // 打印错误信息，提示从服务器接收到无效的响应  
                    System.out.println("Invalid response: " + response);  
                    continue;
                }

                // Extract the filename from the response parts at index 1
                // 从响应数据中提取文件名在索引1位置  
                String responseFilename = parts[1]; 

                // Parse the file size from the response parts  at index 3 into a long value  
                // 将响应数据中的文件大小在索引3位置解析为 long 类型  
                long fileSize = Long.parseLong(parts[3]); 

                // Parse the data port number from the response parts at index 5 into an integer  
                // 将响应数据中的数据端口号在索引5位置解析为 int 类型  
                int dataPort = Integer.parseInt(parts[5]);  

                // Print download information including filename and file size  
                // 打印下载信息，包含文件名和文件大小  
                System.out.printf("Downloading %s (size: %d bytes)%n", responseFilename, fileSize); 


                // Create a File object representing the output file with the given filename
                // 根据给定的文件名创建一个表示输出文件的File对象
                File outputFile = new File(filename);

                // Try-with-resources block to open a RandomAccessFile for reading and writing
                // 使用try-with-resources语句块打开一个可读写的RandomAccessFile
                try (RandomAccessFile raf = new RandomAccessFile(outputFile, "rw")) {
                    // Atomic counter for received bytes
                    // 用于记录接收字节数的原子计数器
                    AtomicInteger bytesReceived = new AtomicInteger(0);

                    // Loop through the file in chunks of MAX_BLOCK_SIZE bytes
                    // 以MAX_BLOCK_SIZE字节为块大小循环遍历文件
                    for (long start = 0; start < fileSize; start += MAX_BLOCK_SIZE){
                        // Calculate the end position for the current block, ensuring we don't exceed file size
                        // 计算当前块的结束位置，确保不超过文件大小
                        long end = Math.min(start + MAX_BLOCK_SIZE - 1, fileSize - 1);

                        // Create block request message in the format: "FILE [filename] GET START [start] END [end]"
                        // 创建块请求消息，格式为："FILE [文件名] GET START [起始位置] END [结束位置]"
                        String blockRequest = String.format("FILE %s GET START %d END %d", filename, start, end);

                        // Send block request and get response
                        // 发送块请求并获取响应
                        String blockResponse = sendAndReceive(socket, serverAddress, dataPort, blockRequest);

                        // Check if the block response is null 
                        // 检查块响应是否为null
                        if (blockResponse == null){
                            // Print error message indicating block retrieval failure after retry attempts
                            // 打印错误信息，表示重试后仍然获取数据块失败
                            System.out.println("Failed to get block after retries");
                            break;
                        }

                        // Split block response (limit split to preserve data)
                        // 拆分块响应（限制拆分以保留数据）
                        String[] blockParts = blockResponse.split(" ", 8);

                        // Verify the block response starts with "FILE" and contains "OK" status
                        // 验证块响应是否以"FILE"开头且包含"OK"状态
                        if (!blockParts[0].equals("FILE") || !blockParts[2].equals("OK")){
                            // Print the invalid response message for debugging
                            // 打印无效响应消息用于调试
                            System.out.println("Invalid block response: " + blockResponse);
                            break;
                        }

                        // Extract the Base64 encoded data portion after the "DATA" marker in the response
                        // 从响应中提取"DATA"标记后的Base64编码数据部分
                        String base64Data = blockResponse.substring(blockResponse.indexOf("DATA") + 5);

                        // Decode the Base64 string into binary file data
                        // 将Base64字符串解码为二进制文件数据
                        byte[] fileData = Base64.getDecoder().decode(base64Data);

                        // Move the file pointer to the correct position for this block
                        // 将文件指针移动到当前数据块的正确位置
                        raf.seek(start);

                        // Write the decoded binary data to the file
                        // 将解码后的二进制数据写入文件
                        raf.write(fileData);

                        // Update the total bytes received counter atomically
                        // 原子性地更新已接收字节总数
                        bytesReceived.addAndGet(fileData.length);

                        // Print progress indicator for each successfully received block
                        // 为每个成功接收的数据块打印进度指示器
                        System.out.print("*");

                    }

                    // Print new line after progress indicators
                    // 在进度指示器后打印新行
                    System.out.println();

                    // Print summary of completed download with total bytes received
                    // 打印下载完成摘要及接收到的总字节数
                    System.out.printf("Downloaded %d bytes of %s%n", bytesReceived.get(), filename);

                    // Send CLOSE message to confirm completion
                    // 发送CLOSE消息确认完成
                    String closeMsg = "FILE " + filename + " CLOSE";

                    // Send close message and receive server's response for the data connection
                    // 发送关闭消息并接收服务器对数据连接的响应
                    String closeResponse = sendAndReceive(socket, serverAddress, dataPort, closeMsg);

                    // Check if close response is valid 
                    // 检查关闭响应是否有效
                    if (closeResponse != null && closeResponse.startsWith("FILE") && closeResponse.contains("CLOSE_OK")) {
                        // Print success message if close confirmation is received
                        // 如果收到关闭确认，打印成功消息
                        System.out.println("File transfer completed successfully");
                    }
                    else {
                        // Print error message if close confirmation is missing or invalid
                        // 如果关闭确认缺失或无效，打印错误消息
                        System.out.println("Failed to get proper close confirmation");
                    }
                    

                }

            }

        }catch (Exception e) {
            // Print user-friendly error message with exception details
            // 打印包含异常详情的用户友好错误信息
            System.out.println("Client error: " + e.getMessage());
            e.printStackTrace();
        }

    }

    // Helper method to send a message and receive response with retries
    // 辅助方法，用于发送消息并在重试机制下接收响应
    private static String sendAndReceive(DatagramSocket socket, InetAddress address, int port, String message) throws IOException{
        // Convert message to bytes and create send packet
        // 将消息转换为字节并创建发送包
        byte[] sendData = message.getBytes();

        // Create a UDP datagram packet for sending data
        // 创建一个用于发送数据的UDP数据报包
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);

        // Create receive packet buffer
        // 创建接收包缓冲区
        byte[] receiveData = new byte[2048];

        // Create a UDP datagram packet for receiving incoming data  
        // 创建一个用于接收传入数据的UDP数据报包
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

        // Initialize timeout and retry counters
        // 初始化超时和重试计数器
        int currentTimeout = INITIAL_TIMEOUT;
        int retries = 0;


        // Retry loop
        // 重试循环
        while (retries < MAX_RETRIES){
            try{
                // Send the prepared UDP datagram packet
                // 发送已准备好的UDP数据报包
                socket.send(sendPacket);
                // Set the socket timeout for subsequent receive operations 
                // 为后续的接收操作设置套接字超时时间
                socket.setSoTimeout(currentTimeout);

                try {
                    // Receive an incoming UDP packet 
                    // 接收传入的UDP数据包
                    socket.receive(receivePacket);
                    
                } catch (Exception e) {
                }

            }
        }

    }

}




