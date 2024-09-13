package com.etyc.user.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;

import java.io.Serial;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author Etsuya
 * @since 2024-08-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("group")
public class Group implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 唯一标识符，主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户的ID
     */
    private Long userId;

    /**
     * 记录创建时间
     */
    private LocalDateTime createTime;

    /**
     * 记录最后更新时间
     */
    private LocalDateTime updateTime;


}
