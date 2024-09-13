package com.etyc.chat.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.Comparator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author Etsuya
 * @since 2024-08-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("personal_msg")
@Builder
public class PersonalMsg implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 唯一标识符，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 消息发送者的ID
     */
    private Long sender;

    /**
     * 消息接收者的ID
     */
    private Long receiver;

    /**
     * 消息类型：0 表示文本消息，1 表示文件消息（链接）
     */
    private Integer type;

    /**
     * 消息内容，文本或文件链接
     */
    private String content;

	private String meta;

    /**
     * 记录创建时间
     */
    private LocalDateTime createTime;

    /**
     * 记录最后更新时间
     */
    private LocalDateTime updateTime;

    public static Comparator<PersonalMsg> oldFirstComparator = (o1, o2) -> {
		if(o1.getCreateTime().isEqual(o2.getCreateTime())){
			return (int) (o1.getId() - o2.getId());
		} else {
			return o1.getCreateTime().isBefore(o2.getCreateTime())? -1: 1;
		}
	};

}
