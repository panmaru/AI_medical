package com.medical;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AI智能问诊医疗辅助平台 - 主启动类
 *
 * @author AI Medical Team
 * @version 1.0.0
 */
@SpringBootApplication
@MapperScan("com.medical.mapper")
public class AiMedicalApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiMedicalApplication.class, args);
        System.out.println("""

                =======================================================
                   ____  _____    _    ____  _____    _    ____ ___
                  |  _ \\| ____|  / \\  |  _ \\| ____|  / \\  |  _ \\_ _|
                  | |_) |  _|   / _ \\ | |_) |  _|   / _ \\ | |_) | |
                  |  _ <| |___ / ___ \\|  _ <| |___ / ___ \\|  __/| |
                  |_| \\_\\_____/_/   \\_\\_| \\_\\_____/_/   \\_\\_|  |___|

                AI智能问诊医疗辅助平台启动成功！
                访问地址: http://localhost:8080
                =======================================================
                """);
    }

}
