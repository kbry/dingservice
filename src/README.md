########### 环境依赖
1. JDK x.x.x


########### 部署步骤
1. 参考网上安装JDK步骤


########### 目录结构描述
├── src
│   ├── main
│       └── java
│           └── com.sangfor
│               └── api
│                   └── CreateLocalUser.class  // 创建本地用户示例
│               └── common
│                   ├── CommonResponse.class   // 公共响应Bean
│                   ├── CommonUtil.class       // 公共工具类
│                   ├── Config.class           // 公共配置文件
│                   ├── ConsoleCrypto.class    // 控制台公钥信息Bean
│                   ├── ConsoleResponse.class  // 控制台信息Bean
│                   └── GroupResponse.class    // 组织架构信息Bean
│               └── vo
│                   └── LocalUserVo.class      // 本地用户Vo
│               └── Main.class                 // 程序主入口
│   ├── Readme.md               // help
│   └── pom.xml                 // maven描述文件（包含所需要的包信息）



########### 运行说明
注意：为了避免对当前业务造成影响，请不要直接在生产环境下执行demo代码
1. 登录控制台，进入“系统管理->系统运维->Open API”，新建您的Open API设备，获取API ID和API密码
2. 在config.js文件中配置您的控制台地址(CONSOLE_ADDRESS)、API ID(API_ID)以及API密码(API_SECRET) 
3. nodejs执行对应demo的.js文件，node ./create_local_user.py
