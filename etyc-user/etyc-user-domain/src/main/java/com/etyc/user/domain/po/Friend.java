package com.etyc.user.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;

import java.io.Serial;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import lombok.AllArgsConstructor;
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
 * @since 2024-08-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("friend")
public class Friend implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public Friend(Long userIdA, Long userIdB){
        this.userIdA = userIdA;
        this.userIdB = userIdB;
    }

    /**
     * 唯一标识符，主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户A的ID
     */
    private Long userIdA;

    /**
     * 用户B的ID
     */
    private Long userIdB;

    /**
     * 记录创建时间
     */
    private LocalDateTime createTime;

    /**
     * 记录最后更新时间
     */
    private LocalDateTime updateTime;
}
