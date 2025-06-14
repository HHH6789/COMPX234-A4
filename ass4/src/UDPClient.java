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



            }

        }

    }

}




