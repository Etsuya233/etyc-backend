package com.etyc.user.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;

import java.io.Serial;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author Etsuya
 * @since 2024-08-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user")
public class User implements Serializable, Comparable<User> {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID，主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户名，唯一
     */
    @NotBlank(message = "用户名不得为空！")
    @Length(min = 6, max = 30, message = "用户名只能在6-30字之间！")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "用户名只能包含字母、数字、下划线、中划线！")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不得为空！")
    @Length(min = 8, max = 30, message = "密码只能在8-30字之间！")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&,])[A-Za-z\\d@$!%*?&,]+$",
            message = "密码至少包含大写字母、小写字母、数字、特殊字符！")
    private String password;

    /**
     * 昵称
     */
    @NotBlank(message = "昵称不得为空！")
    @Length(min = 1, max = 30, message = "昵称只能在1-30字之间！")
    private String nickname;

    /**
     * 生日
     */
    private LocalDate birthday;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 手机号码，唯一
     */
    private String phone;

    /**
     * 邮箱地址，唯一
     */
    @Email
    private String email;

    /**
     * 性别：1-男，0-女，2-未知
     */
    private Integer sex;

    /**
     * 头像路径
     */
    private String avatar;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 用户状态：0-正常，1-禁用，2-被删除
     */
    private Integer status;

    /**
     * 用户说明
     */
    private String statusDesc;


    @Override
    public int compareTo(User o) {
        return this.getUsername().compareTo(o.getUsername());
    }
}
