package com.example.tea.aop;

import com.example.tea.annotation.OperLog;
import com.example.tea.entity.pojo.SysOperLog;
import com.example.tea.mapper.SysOperLogMapper;
import com.example.tea.mapper.UserMapper;
import com.example.tea.utils.ThreadLocalUserIdUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
public class OperLogAspect {

    @Autowired
    private SysOperLogMapper operLogMapper;
    @Autowired
    private UserMapper userMapper;
    /** 统一使用 Spring Boot 配置好的 ObjectMapper（含 JavaTime 等模块），避免各处 new。 */
    @Autowired
    private ObjectMapper objectMapper;

    @Pointcut("@annotation(com.example.tea.annotation.OperLog)")
    public void operLogPointCut() {}

    @Around("operLogPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        SysOperLog log = new SysOperLog();
        log.setStatus(1);
        log.setOperTime(LocalDateTime.now());

        try {
            setOperLogInfo(joinPoint, log);
            return joinPoint.proceed();
        } catch (Exception e) {
            log.setStatus(0);
            log.setErrorMsg(e.getMessage());
            throw e;
        } finally {
            operLogMapper.insert(log);
        }
    }

    private void setOperLogInfo(ProceedingJoinPoint joinPoint, SysOperLog log) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperLog operLog = method.getAnnotation(OperLog.class);

        log.setOperModule(operLog.module());
        log.setOperType(operLog.type());

        // 方法路径
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = method.getName();
        log.setRequestMethod(className + "." + methodName);

        // 请求参数
        Object[] args = joinPoint.getArgs();
        try {
            // Jackson 序列化请求参数
            String params = objectMapper.writeValueAsString(args);
            log.setRequestParam(params);
        } catch (Exception e) {
            log.setRequestParam("参数序列化失败");
        }
        log.setOperName(userMapper.getNameByUserId(ThreadLocalUserIdUtil.getCurrentId()));
    }
}
