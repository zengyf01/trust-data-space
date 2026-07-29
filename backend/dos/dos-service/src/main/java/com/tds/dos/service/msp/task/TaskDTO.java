package com.tds.dos.service.msp.task;

import com.tds.dos.common.enums.TaskStatus;
import com.tds.dos.common.enums.TaskType;
import java.util.List;
import java.util.Map;

/**
 * Task DTO for creation and updates
 */
public class TaskDTO {
    private String taskId;
    private String name;
    private TaskType type;
    private String algorithm;
    private List<String> participants;
    private Map<String, Object> inputs;
    private Map<String, String> parameters;
    private String description;
    private String nodeMode;
    private String creator;

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public TaskType getType() { return type; }
    public void setType(TaskType type) { this.type = type; }
    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }
    public Map<String, Object> getInputs() { return inputs; }
    public void setInputs(Map<String, Object> inputs) { this.inputs = inputs; }
    public Map<String, String> getParameters() { return parameters; }
    public void setParameters(Map<String, String> parameters) { this.parameters = parameters; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getNodeMode() { return nodeMode; }
    public void setNodeMode(String nodeMode) { this.nodeMode = nodeMode; }
    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }
}
