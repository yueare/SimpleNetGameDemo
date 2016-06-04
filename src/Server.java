/**
 * Created by FJU on 2015/5/20.
 */
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Server extends JFrame {

    //障碍物 ID
    private  static  int obstacleID =-1;
    //UI组件
    private static JTextArea jta = new JTextArea();
    //服务器通信对象
    private ServerSocket server;
    //在线玩家列表
    private static List<Socket> list = new ArrayList<Socket>();
    //玩家控制单位数据元列表
    private static List<Points> playerObj = new ArrayList<Points>();
    //自动移动障碍物列表
    private static List<Points> obstacleObj = new ArrayList<Points>();
    //客户端线程池
    private ExecutorService threadpool;
    //全局碰撞判定线程
    private static Thread judge_thread;
    //全局障碍调整线程
    private static Thread obstacle_adjust;
    //临时颜色表
    private static Color [] colors = new Color[5];
    //障碍调整具体方案
    private Runnable adjust = new Runnable() {
        @Override
        public void run() {
            int step=2;//移动步长

            while (true)
            {
                try {
                    //障碍物移动规则
                    for(int i=0;i<obstacleObj.size();i++)
                    {
                        //int goX =(int)(Math.random()*3);
                        //int goY =(int)(Math.random()*3);
                        if(obstacleObj.get(i).getColor().equals(Color.cyan))
                        {
                            step=2;
                        }
                        else if(obstacleObj.get(i).getColor().equals(Color.green))
                        {
                            step=3;
                        }
                        else if(obstacleObj.get(i).getColor().equals(Color.blue))
                        {
                            step=5;
                        }
                        else if(obstacleObj.get(i).getColor().equals(Color.red))
                        {
                            step=6;
                        }
                        else if(obstacleObj.get(i).getColor().equals(Color.orange))
                        {
                            step=3;
                        }
                        obstacleObj.get(i).y+=step;

                        if(obstacleObj.get(i).y>560)
                        {
                            obstacleObj.get(i).y=-2;
                            obstacleObj.get(i).x= (int)(Math.random()*70)*10;
                        }


                    }
                    //发送处理结果-障碍物
                    String finalData ="";
                    for(int i=0;i< obstacleObj.size();i++)
                    {
                        finalData += obstacleObj.get(i).ID+","+ obstacleObj.get(i).x+","+ obstacleObj.get(i).y+","+ obstacleObj.get(i).r+","+ obstacleObj.get(i).g+","+ obstacleObj.get(i).b+","+ obstacleObj.get(i).size+","+0;
                        if(i!= obstacleObj.size()-1)
                        {
                            finalData +=":";
                        }
                    }
                    //发送数据
                    for(int i=0;i<list.size();i++)
                    {
                        Socket client=list.get(i);
                        DataOutputStream toclient= new DataOutputStream(client.getOutputStream());
                        toclient.writeUTF(finalData);
                    }

                    obstacle_adjust.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();

                } catch (IOException e) {
                    e.printStackTrace();

                }
            }


        }
    };

    public Server(){
        //颜色表初始化
        colors[0] =Color.cyan;
        colors[1] =Color.green;
        colors[2] =Color.blue;
        colors[3] =Color.red;
        colors[4] =Color.orange;
        //布局初始化
        setLayout(new BorderLayout());
        add(new JScrollPane(jta), BorderLayout.CENTER);

        setTitle("数据处理服务器");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        //服务器全局单位判定线程
        judge_thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true)
                {

                    try
                    {
                        if(playerObj.size()>0)
                        {
                            //障碍物碰撞检测
                            obstacleCollision();
                            //数据发送
                            Task.sendData();
                        }
                        judge_thread.sleep(10);
                        if(!obstacle_adjust.isAlive())//如果线程意外死亡，可以重新开启线程，损耗资源比较大。
                        {
                            //obstacle_adjust =new Thread(adjust);
                            //obstacle_adjust.start();
                            System.out.print("start obstacle_adjust\n");
                        }
                    }
                    catch (Exception e) {}
                }

            }
        });
        judge_thread.start();

        //初始化obstacleObj列表
        for(int i=0;i<20;i++)
        {
            int newX =  (int)(Math.random()*80)*10;
            int newY =  (int)(Math.random()*50)*10;
            int cr =  (int)(Math.random()*5);
            int r =colors[cr].getRed();
            int g =colors[cr].getGreen();
            int b =colors[cr].getBlue();
            Points newobstacle = new Points(obstacleID,newX,newY,r,g,b,10,0);
            obstacleObj.add(newobstacle);
        }
        //服务器全局障碍物调整线程
        obstacle_adjust = new Thread(adjust);
        obstacle_adjust.start();


        try{
            //服务器主线程----------------------------------------------------------------------------------监听客户
            server = new ServerSocket(54321);
            threadpool = Executors.newCachedThreadPool();
            jta.append("服务器已经启动\n");

            Socket client = null;
            while(true){//------------------------------------main Thread
                client =server.accept();
                list.add(client);
                threadpool.execute(new Task(client));
                System.out.println("running now ..............client num:" + list.size());
            }
        }catch(Exception e){}


    }

    //每个客户端执行的线程内容
    static class Task implements Runnable{
        private Socket client;
        private DataInputStream fromclient;
        //private DataOutputStream toclient;
        String msg;

        public Task(Socket client) throws IOException{
            this.client = client;
            fromclient = new DataInputStream(client.getInputStream());
        }
        public void run() {
            try{
                //接收数据
                while((msg=fromclient.readUTF())!=null){

                    jta.setSelectionStart(jta.getText().length());
                    //解析数据
                    String s[] = msg.split(",");
                    if(s[1].equals("connected"))//连接反馈
                    {
                        jta.append("[" + client.getPort() + "]request:" + "Connected Successfully Online client:" +list.size() + "\n");
                        //连接成功后为客户端创建控制对象，坐标为随机位置
                        int newX =  (int)(Math.random()*70)*10;
                        int newY =  (int)(Math.random()*50)*10;
                        int cr =  (int)(Math.random()*5);
                        int r =colors[cr].getRed();
                        int g =colors[cr].getGreen();
                        int b =colors[cr].getBlue();
                        Points px = new Points(Integer.valueOf(s[0]),newX,newY,r,g,b,10,0);
                        playerObj.add(px);
                        sendData();//数据发回客户端
                        continue;
                    }
                    if(s[1].equals("exit"))//退出反馈
                    {

                        removeObj(s[0]);
                        sendData();
                        jta.append("[" + client.getPort() + "]request:" + "Disconnected Online client:" +list.size() + "\n");
                        continue;
                    }
                    if(s.length>8&&s[8].equals("reset"))//重新开始反馈
                    {
                        jta.append("[" + client.getPort() + "]request:" + "Reset" + "\n");
                        reset(s[0]);//重置数据
                        sendData();
                        continue;
                    }
                    for(int i=0;i< playerObj.size();i++)//更新对应ID的玩家数据
                    {
                        if(playerObj.get(i).ID==Integer.valueOf(s[0]))
                        {
                            Points p = new Points(Integer.valueOf(s[0]),Integer.valueOf(s[1]),Integer.valueOf(s[2]),Integer.valueOf(s[3]),Integer.valueOf(s[4]),Integer.valueOf(s[5]),Integer.valueOf(s[6]),Integer.valueOf(s[7]));
                            playerObj.set(i, p);
                            break;
                        }

                    }
                    sendData();//发回数据
                }
            }catch(Exception e){}
        }
        public static void sendData() throws IOException{
            //碰撞处理
            processCollision();


            //处理数据
            String finalData ="";
            for(int i=0;i< playerObj.size();i++)
            {
                //数据封装
                finalData += playerObj.get(i).ID+","+ playerObj.get(i).x+","+ playerObj.get(i).y+","+ playerObj.get(i).r+","+ playerObj.get(i).g+","+ playerObj.get(i).b+","+ playerObj.get(i).size+","+playerObj.get(i).score;
                if(i!= playerObj.size()-1)
                {
                    finalData +=":";
                }
            }
            //发送数据
            for(Socket client:list){

                DataOutputStream toclient= new DataOutputStream(client.getOutputStream());
                toclient.writeUTF(finalData);
            }
        }

        public static void processCollision()//玩家单位碰撞检测，用于2个玩家体积交换
        {
            //int k=0;

            for(int i=0;i< playerObj.size();i++)
            {
                for(int j=i+1;j< playerObj.size();j++)
                {
                    Rectangle tmp1,tmp2;
                    tmp1 = new Rectangle();
                    tmp2 = new Rectangle();
                    tmp1.setBounds(playerObj.get(i).x, playerObj.get(i).y, playerObj.get(i).getSize(), playerObj.get(i).getSize());
                    tmp2.setBounds(playerObj.get(j).x, playerObj.get(j).y, playerObj.get(j).getSize(), playerObj.get(j).getSize());
                    if(tmp1.intersects(tmp2)||tmp2.intersects(tmp1))
                    {

                        playerObj.get(i).size= tmp2.width;
                        playerObj.get(j).size= tmp1.width;
                        //碰撞弹开,模拟
                        if(playerObj.get(i).x> playerObj.get(j).x)
                        {
                            playerObj.get(i).x+=2;
                            playerObj.get(j).x-=2;
                        }
                        else
                        {
                            playerObj.get(i).x-=2;
                            playerObj.get(j).x+=2;
                        }

                        if(playerObj.get(i).y> playerObj.get(j).y)
                        {
                            playerObj.get(i).y+=2;
                            playerObj.get(j).y-=2;
                        }
                        else
                        {
                            playerObj.get(i).y-=2;
                            playerObj.get(j).y+=2;
                        }

                        //System.out.println("change size now"+ playerObj.get(j).ID +"," +playerObj.get(j+1).ID);
                        //System.out.println(playerObj.get(i).ID+" 碰撞 "+playerObj.get(j).ID);
                    }
                    //System.out.println(++k);
                }

            }
        }




        private void removeObj(String ID)//移除玩家在客户端的所有数据
        {
            int id=Integer.valueOf(ID);
            int loop=list.size();

            int pos=-1;
            for(int j=0;j<loop;j++)
            {
                if (list.get(j).getPort()==id)//查找退出的ID
                {
                    pos=j;
                }
            }
            if(pos!=-1)
            {
                list.remove(pos);//移除client 对象
                System.out.print("---" + id + "----断开服务器");
            }

            loop= playerObj.size();
            int index=-1;
            for(int i=0;i<loop;i++)
            {
                if(playerObj.get(i).ID==id)//查找对应ID 的 obj
                {
                    index=i;
                }
            }
            if(index!=-1)
            {
                playerObj.remove(index);//移除数据单位
                System.out.print("--已经移除控制单位\n");
                System.out.println("running now ..............client num:" + list.size());
                System.out.println("pointData size=" + playerObj.size());
            }

        }

        private void reset(String ID)//重置玩家数据
        {
            int id=Integer.valueOf(ID);
            int loop= playerObj.size();
            for(int i=0;i<loop;i++)
            {
                if(playerObj.get(i).ID==id)//查找请求重置的玩家ID
                {
                    playerObj.get(i).size=10;
                    int cr =  (int)(Math.random()*5);
                    int r =colors[cr].getRed();
                    int g =colors[cr].getGreen();
                    int b =colors[cr].getBlue();

                    playerObj.get(i).r=r;
                    playerObj.get(i).g=g;
                    playerObj.get(i).b=b;
                    playerObj.get(i).score=0;
                    break;
                }
            }
        }
    }

    public void obstacleCollision()//障碍物碰撞检测
    {
        Rectangle ptmp,otmp;
        ptmp = new Rectangle();
        otmp = new Rectangle();
        for (int i=0;i<playerObj.size();i++)
        {
            ptmp.setBounds(playerObj.get(i).x, playerObj.get(i).y, playerObj.get(i).getSize(), playerObj.get(i).getSize());
            for(int j=0;j<obstacleObj.size();j++)
            {
                otmp.setBounds(obstacleObj.get(j).x, obstacleObj.get(j).y, 20, 20);
                if(ptmp.intersects(otmp)||otmp.intersects(ptmp))
                {
                    if(playerObj.get(i).size<80)
                    {
                        if(playerObj.get(i).getColor().equals(obstacleObj.get(j).getColor()))//相同类型 处理玩家数据
                        {
                            if( playerObj.get(i).size>10)
                            {
                                playerObj.get(i).size-=5;
                            }
                            playerObj.get(i).score++;//分数 +1



                        }
                        else//不同类型 处理
                        {
                            playerObj.get(i).size+=5;


                        }
                        //障碍物出界重置
                        obstacleObj.get(j).y=-2;
                        int cr =  (int)(Math.random()*5);
                        int r =colors[cr].getRed();
                        int g =colors[cr].getGreen();
                        int b =colors[cr].getBlue();
                        obstacleObj.get(j).r=r;
                        obstacleObj.get(j).g=g;
                        obstacleObj.get(j).b=b;


                    }


                }
            }
        }
    }
    public static void main(String[] args){
        new Server();
    }

}