package com.mcreater.amclcore.concurrent;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaskState<T> {
    private Throwable throwable;
    private T data;
}
