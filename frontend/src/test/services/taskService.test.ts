import axios from 'axios';

import { TaskService } from '@/main/services/taskService';
import { Task, TaskRequest, TaskStatus } from '@/main/types/task';

jest.mock('axios');
const mockedAxios = axios as jest.Mocked<typeof axios>;

describe('TaskService', () => {
  let taskService: TaskService;
  let mockAxiosInstance: any;

  beforeEach(() => {
    mockAxiosInstance = {
      get: jest.fn(),
      post: jest.fn(),
      put: jest.fn(),
      delete: jest.fn(),
    };

    mockedAxios.create.mockReturnValue(mockAxiosInstance);

    taskService = new TaskService();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  describe('constructor', () => {
    it('should create axios instance with correct config', () => {
      expect(mockedAxios.create).toHaveBeenCalledWith({
        baseURL: 'http://backend:8080/api',
        timeout: 1000,
        headers: {
          'Content-Type': 'application/json',
        },
      });
    });

    it('should use API_BASE_URL from environment if available', () => {
      process.env.API_BASE_URL = 'http://custom-api:3000';
      new TaskService();

      expect(mockedAxios.create).toHaveBeenCalledWith(
        expect.objectContaining({
          baseURL: 'http://custom-api:3000',
        })
      );

      delete process.env.API_BASE_URL;
    });
  });

  describe('getAllTasks', () => {
    it('should fetch all tasks successfully', async () => {
      const mockTasks: Task[] = [
        {
          id: 1,
          title: 'Task 1',
          description: 'First task description',
          status: TaskStatus.TODO,
          dueDateTime: '2024-12-31T23:59:59Z',
          createdAt: '2024-01-01T10:00:00Z',
          updatedAt: '2024-01-01T10:00:00Z',
        },
        {
          id: 2,
          title: 'Task 2',
          status: TaskStatus.IN_PROGRESS,
          createdAt: '2024-01-02T10:00:00Z',
          updatedAt: '2024-01-03T15:30:00Z',
        },
      ];

      mockAxiosInstance.get.mockResolvedValue({ data: mockTasks });

      const result = await taskService.getAllTasks();

      expect(mockAxiosInstance.get).toHaveBeenCalledWith('/tasks/get-all-tasks');
      expect(result).toEqual(mockTasks);
    });

    it('should handle errors when fetching tasks fails', async () => {
      const error = new Error('Network error');
      mockAxiosInstance.get.mockRejectedValue(error);

      await expect(taskService.getAllTasks()).rejects.toThrow('Network error');
    });

    it('should return empty array when no tasks exist', async () => {
      mockAxiosInstance.get.mockResolvedValue({ data: [] });

      const result = await taskService.getAllTasks();

      expect(result).toEqual([]);
    });
  });

  describe('getTaskById', () => {
    it('should fetch a single task by id', async () => {
      const mockTask: Task = {
        id: 1,
        title: 'Test Task',
        description: 'Test description',
        status: TaskStatus.TODO,
        dueDateTime: '2024-12-31T23:59:59Z',
        createdAt: '2024-01-01T10:00:00Z',
        updatedAt: '2024-01-01T10:00:00Z',
      };

      mockAxiosInstance.get.mockResolvedValue({ data: mockTask });

      const result = await taskService.getTaskById(1);

      expect(mockAxiosInstance.get).toHaveBeenCalledWith('/tasks/1/get-task');
      expect(result).toEqual(mockTask);
    });

    it('should handle 404 errors', async () => {
      const error = { response: { status: 404 } };
      mockAxiosInstance.get.mockRejectedValue(error);

      await expect(taskService.getTaskById(999)).rejects.toEqual(error);
    });
  });

  describe('createTask', () => {
    it('should create a new task with all fields', async () => {
      const taskRequest: TaskRequest = {
        title: 'New Task',
        description: 'Task description',
        status: TaskStatus.TODO,
        dueDateTime: '2024-12-31T23:59:59Z',
      };

      const createdTask: Task = {
        id: 3,
        title: 'New Task',
        description: 'Task description',
        status: TaskStatus.TODO,
        dueDateTime: '2024-12-31T23:59:59Z',
        createdAt: '2024-01-15T10:00:00Z',
        updatedAt: '2024-01-15T10:00:00Z',
      };

      mockAxiosInstance.post.mockResolvedValue({ data: createdTask });

      const result = await taskService.createTask(taskRequest);

      expect(mockAxiosInstance.post).toHaveBeenCalledWith(
        '/tasks/create-task',
        taskRequest
      );
      expect(result).toEqual(createdTask);
    });

    it('should create a task with minimal fields', async () => {
      const taskRequest: TaskRequest = {
        title: 'Minimal Task',
        status: TaskStatus.TODO,
      };

      const createdTask: Task = {
        id: 4,
        title: 'Minimal Task',
        status: TaskStatus.TODO,
        createdAt: '2024-01-15T10:00:00Z',
        updatedAt: '2024-01-15T10:00:00Z',
      };

      mockAxiosInstance.post.mockResolvedValue({ data: createdTask });

      const result = await taskService.createTask(taskRequest);

      expect(result).toEqual(createdTask);
    });

    it('should handle validation errors', async () => {
      const taskRequest: TaskRequest = {
        title: '',
        status: TaskStatus.TODO,
      };
      const error = {
        response: {
          status: 400,
          data: { message: 'Title cannot be empty' },
        },
      };

      mockAxiosInstance.post.mockRejectedValue(error);

      await expect(taskService.createTask(taskRequest)).rejects.toEqual(error);
    });
  });

  describe('updateTask', () => {
    it('should update an existing task', async () => {
      const taskRequest: TaskRequest = {
        title: 'Updated Task',
        description: 'Updated description',
        status: TaskStatus.IN_PROGRESS,
        dueDateTime: '2024-12-31T23:59:59Z',
      };

      const updatedTask: Task = {
        id: 1,
        title: 'Updated Task',
        description: 'Updated description',
        status: TaskStatus.IN_PROGRESS,
        dueDateTime: '2024-12-31T23:59:59Z',
        createdAt: '2024-01-01T10:00:00Z',
        updatedAt: '2024-01-15T14:30:00Z',
      };

      mockAxiosInstance.put.mockResolvedValue({ data: updatedTask });

      const result = await taskService.updateTask(1, taskRequest);

      expect(mockAxiosInstance.put).toHaveBeenCalledWith(
        '/tasks/1/update-task',
        taskRequest
      );
      expect(result).toEqual(updatedTask);
    });
  });

  describe('updateTaskStatus', () => {
    it('should update task status to COMPLETED', async () => {
      const updatedTask: Task = {
        id: 1,
        title: 'Task',
        status: TaskStatus.COMPLETED,
        createdAt: '2024-01-01T10:00:00Z',
        updatedAt: '2024-01-15T16:00:00Z',
      };

      mockAxiosInstance.put.mockResolvedValue({ data: updatedTask });

      const result = await taskService.updateTaskStatus(1, TaskStatus.COMPLETED);

      expect(mockAxiosInstance.put).toHaveBeenCalledWith(
        '/tasks/1/status?status=COMPLETED'
      );
      expect(result).toEqual(updatedTask);
    });

    it('should update task status to CANCELLED', async () => {
      const updatedTask: Task = {
        id: 2,
        title: 'Cancelled Task',
        status: TaskStatus.CANCELLED,
        createdAt: '2024-01-01T10:00:00Z',
        updatedAt: '2024-01-15T16:00:00Z',
      };

      mockAxiosInstance.put.mockResolvedValue({ data: updatedTask });

      const result = await taskService.updateTaskStatus(2, TaskStatus.CANCELLED);

      expect(mockAxiosInstance.put).toHaveBeenCalledWith(
        '/tasks/2/status?status=CANCELLED'
      );
      expect(result).toEqual(updatedTask);
    });

    it('should handle invalid status transitions', async () => {
      const error = {
        response: {
          status: 400,
          data: { message: 'Cannot transition from COMPLETED to IN_PROGRESS' },
        },
      };

      mockAxiosInstance.put.mockRejectedValue(error);

      await expect(
        taskService.updateTaskStatus(1, TaskStatus.IN_PROGRESS)
      ).rejects.toEqual(error);
    });
  });

  describe('deleteTask', () => {
    it('should delete a task', async () => {
      mockAxiosInstance.delete.mockResolvedValue({ status: 204 });

      await taskService.deleteTask(1);

      expect(mockAxiosInstance.delete).toHaveBeenCalledWith('/tasks/1/delete-task');
    });

    it('should handle deletion of non-existent task', async () => {
      const error = {
        response: {
          status: 404,
          data: { message: 'Task not found' },
        },
      };
      mockAxiosInstance.delete.mockRejectedValue(error);

      await expect(taskService.deleteTask(999)).rejects.toEqual(error);
    });

    it('should handle deletion of task with dependencies', async () => {
      const error = {
        response: {
          status: 409,
          data: { message: 'Cannot delete task with active dependencies' },
        },
      };
      mockAxiosInstance.delete.mockRejectedValue(error);

      await expect(taskService.deleteTask(1)).rejects.toEqual(error);
    });
  });

  describe('error handling', () => {
    it('should handle network timeouts', async () => {
      const timeoutError = new Error('timeout of 1000ms exceeded');
      mockAxiosInstance.get.mockRejectedValue(timeoutError);

      await expect(taskService.getAllTasks()).rejects.toThrow('timeout of 1000ms exceeded');
    });

    it('should handle server errors', async () => {
      const serverError = {
        response: {
          status: 500,
          data: { message: 'Internal Server Error' },
        },
      };
      mockAxiosInstance.get.mockRejectedValue(serverError);

      await expect(taskService.getAllTasks()).rejects.toEqual(serverError);
    });

    it('should handle unauthorized errors', async () => {
      const authError = {
        response: {
          status: 401,
          data: { message: 'Unauthorized' },
        },
      };
      mockAxiosInstance.get.mockRejectedValue(authError);

      await expect(taskService.getAllTasks()).rejects.toEqual(authError);
    });
  });
});
