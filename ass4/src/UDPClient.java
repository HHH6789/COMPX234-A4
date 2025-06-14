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
    }

}




