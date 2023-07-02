package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.entity.MessageReceiving;
import edu.npu.vo.R;
import org.springframework.transaction.annotation.Transactional;

/**
* @author wangminan
* @description 针对表【message_receiving(消息接收表。)】的数据库操作Service
* @createDate 2023-07-02 13:57:20
*/
public interface MessageReceivingService extends IService<MessageReceiving> {

    R getMessageDetail(String id);

    @Transactional(rollbackFor = Exception.class)
    R deleteMessage(String id);
}
