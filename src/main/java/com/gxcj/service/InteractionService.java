package com.gxcj.service;

import com.gxcj.controller.student.InteractionController;

public interface InteractionService {
    Boolean toggleCollection(InteractionController.CollectionReq req);

    Boolean applyJob(InteractionController.ApplyJobReq req);
}
