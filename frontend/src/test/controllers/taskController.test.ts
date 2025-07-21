import { Request, Response, NextFunction } from 'express';

import { TaskController } from '@/main/controllers/taskController';
import { TaskService } from '@/main/services/taskService';
import { Task, TaskStatus } from '@/main/types/task';

jest.mock('@/main/services/taskService');

describe('TaskController', () => {
  let taskController: TaskController;
  let mockTaskService: jest.Mocked<TaskService>;
  let mockRequest: Partial<Request>;
  let mockResponse: Partial<Response>;
  let mockNext: NextFunction;

  beforeEach(() => {
    jest.clearAllMocks();

    mockTaskService = {
      getAllTasks: jest.fn(),
      getTaskById: jest.fn(),
      createTask: jest.fn(),
      updateTask: jest.fn(),
      updateTaskStatus: jest.fn(),
      deleteTask: jest.fn(),
    } as unknown as jest.Mocked<TaskService>;

    (TaskService as jest.MockedClass<typeof TaskService>).mockImplementation(() => mockTaskService);

    taskController = new TaskController();

    mockRequest = {
      params: {},
      body: {},
    };

    mockResponse = {
      render: jest.fn(),
      redirect: jest.fn(),
      json: jest.fn(),
      status: jest.fn().mockReturnThis(),
    };

    mockNext = jest.fn();

    jest.spyOn(console, 'log').mockImplementation(() => {
    });
    jest.spyOn(console, 'error').mockImplementation(() => {
    });
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  describe('index', () => {
    it('should render tasks index page with all tasks', async () => {
      const mockTasks: Task[] = [
        {
          id: 1,
          title: 'Task 1',
          description: 'Description 1',
          status: TaskStatus.TODO,
          createdAt: '2024-01-01T10:00:00Z',
          updatedAt: '2024-01-01T10:00:00Z',
        },
        {
          id: 2,
          title: 'Task 2',
          status: TaskStatus.IN_PROGRESS,
          createdAt: '2024-01-02T10:00:00Z',
          updatedAt: '2024-01-02T10:00:00Z',
        },
      ];

      mockTaskService.getAllTasks.mockResolvedValue(mockTasks);

      await taskController.index(
        mockRequest as Request,
        mockResponse as Response,
        mockNext
      );

      expect(mockTaskService.getAllTasks).toHaveBeenCalled();
      expect(mockResponse.render).toHaveBeenCalledWith('tasks/index', {
        title: 'Tasks',
        tasks: mockTasks,
      });
      expect(mockNext).not.toHaveBeenCalled();
    });

    it('should handle errors by calling next', async () => {
      const error = new Error('Failed to fetch tasks');
      mockTaskService.getAllTasks.mockRejectedValue(error);

      await taskController.index(
        mockRequest as Request,
        mockResponse as Response,
        mockNext
      );

      expect(mockTaskService.getAllTasks).toHaveBeenCalled();
      expect(mockResponse.render).not.toHaveBeenCalled();
      expect(mockNext).toHaveBeenCalledWith(error);
    });

    it('should render empty list when no tasks exist', async () => {
      mockTaskService.getAllTasks.mockResolvedValue([]);

      await taskController.index(
        mockRequest as Request,
        mockResponse as Response,
        mockNext
      );

      expect(mockResponse.render).toHaveBeenCalledWith('tasks/index', {
        title: 'Tasks',
        tasks: [],
      });
    });
  });

  describe('create', () => {
    it('should render create task form with statuses', async () => {
      await taskController.create(
        mockRequest as Request,
        mockResponse as Response,
        mockNext
      );

      expect(mockResponse.render).toHaveBeenCalledWith('tasks/create', {
        title: 'Create task',
        statuses: ['TODO', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'],
      });
      expect(mockNext).not.toHaveBeenCalled();
    });

    it('should handle render errors', async () => {
      const error = new Error('Template error');
      mockResponse.render = jest.fn().mockImplementation(() => {
        throw error;
      });

      await taskController.create(
        mockRequest as Request,
        mockResponse as Response,
        mockNext
      );

      expect(mockNext).toHaveBeenCalledWith(error);
    });
  });

  describe('store', () => {
    it('should create task with all fields and redirect', async () => {
      mockRequest.body = {
        title: 'New Task',
        description: 'Task description',
        status: TaskStatus.TODO,
        dueDateTime: '2024-12-31T23:59:59Z',
      };

      const createdTask: Task = {
        id: 3,
        ...mockRequest.body,
        createdAt: '2024-01-15T10:00:00Z',
        updatedAt: '2024-01-15T10:00:00Z',
      };

      mockTaskService.createTask.mockResolvedValue(createdTask);

      await taskController.store(
        mockRequest as Request,
        mockResponse as Response,
        mockNext
      );

      expect(console.log).toHaveBeenCalledWith('Creating task with data:', mockRequest.body);
      expect(mockTaskService.createTask).toHaveBeenCalledWith({
        title: 'New Task',
        description: 'Task description',
        status: TaskStatus.TODO,
        dueDateTime: '2024-12-31T23:59:59Z',
      });
      expect(mockResponse.redirect).toHaveBeenCalledWith('/tasks');
      expect(mockNext).not.toHaveBeenCalled();
    });

    it('should create task with minimal fields', async () => {
      mockRequest.body = {
        title: 'Minimal Task',
        status: TaskStatus.TODO,
      };

      mockTaskService.createTask.mockResolvedValue({} as Task);

      await taskController.store(
        mockRequest as Request,
        mockResponse as Response,
        mockNext
      );

      expect(mockTaskService.createTask).toHaveBeenCalledWith({
        title: 'Minimal Task',
        description: '',
        status: TaskStatus.TODO,
        dueDateTime: undefined,
      });
      expect(mockResponse.redirect).toHaveBeenCalledWith('/tasks');
    });

    it('should handle empty strings as empty/undefined', async () => {
      mockRequest.body = {
        title: 'Task',
        description: '',
        status: TaskStatus.TODO,
        dueDateTime: '',
      };

      mockTaskService.createTask.mockResolvedValue({} as Task);

      await taskController.store(
        mockRequest as Request,
        mockResponse as Response,
        mockNext
      );

      expect(mockTaskService.createTask).toHaveBeenCalledWith({
        title: 'Task',
        description: '',
        status: TaskStatus.TODO,
        dueDateTime: undefined,
      });
    });

    it('should handle validation errors', async () => {
      mockRequest.body = {
        title: '',
        status: TaskStatus.TODO,
      };

      const error = new Error('Title is required');
      mockTaskService.createTask.mockRejectedValue(error);

      await taskController.store(
        mockRequest as Request,
        mockResponse as Response,
        mockNext
      );

      expect(console.error).toHaveBeenCalledWith('Error in store method:', error);
      expect(mockNext).toHaveBeenCalledWith(error);
      expect(mockResponse.redirect).not.toHaveBeenCalled();
    });
  });

  describe('edit', () => {
    it('should render edit form with task data', async () => {
      mockRequest.params = { id: '1' };

      const mockTask: Task = {
        id: 1,
        title: 'Existing Task',
        description: 'Task to edit',
        status: TaskStatus.IN_PROGRESS,
        dueDateTime: '2024-12-31T23:59:59Z',
        createdAt: '2024-01-01T10:00:00Z',
        updatedAt: '2024-01-10T10:00:00Z',
      };

      mockTaskService.getTaskById.mockResolvedValue(mockTask);

      await taskController.edit(
        mockRequest as Request,
        mockResponse as Response,
        mockNext
      );

      expect(mockTaskService.getTaskById).toHaveBeenCalledWith(1);
      expect(mockResponse.render).toHaveBeenCalledWith('tasks/edit', {
        title: 'Edit Task',
        task: mockTask,
        statuses: ['TODO', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'],
      });
    });

    it('should handle invalid ID format', async () => {
      mockRequest.params = { id: 'abc' };

      const mockTask: Task = {
        id: 0,
        title: 'Task',
        status: TaskStatus.TODO,
        createdAt: '2024-01-01T10:00:00Z',
        updatedAt: '2024-01-01T10:00:00Z',
      };

      mockTaskService.getTaskById.mockResolvedValue(mockTask);

      await taskController.edit(
        mockRequest as Request,
        mockResponse as Response,
        mockNext
      );

      expect(mockTaskService.getTaskById).toHaveBeenCalledWith(NaN);
    });

    it('should handle task not found', async () => {
      mockRequest.params = { id: '999' };

      const error = { response: { status: 404 } };
      mockTaskService.getTaskById.mockRejectedValue(error);

      await taskController.edit(
        mockRequest as Request,
        mockResponse as Response,
        mockNext
      );

      expect(mockTaskService.getTaskById).toHaveBeenCalledWith(999);
      expect(mockNext).toHaveBeenCalledWith(error);
      expect(mockResponse.render).not.toHaveBeenCalled();
    });
  });

  describe('update', () => {
    it('should update task and redirect', async () => {
      mockRequest.params = { id: '1' };
      mockRequest.body = {
        title: 'Updated Task',
        description: 'Updated description',
        status: TaskStatus.COMPLETED,
        dueDateTime: '2024-12-31T23:59:59Z',
      };

      const updatedTask: Task = {
        id: 1,
        ...mockRequest.body,
        createdAt: '2024-01-01T10:00:00Z',
        updatedAt: '2024-01-15T15:00:00Z',
      };

      mockTaskService.updateTask.mockResolvedValue(updatedTask);

      await taskController.update(
        mockRequest as Request,
        mockResponse as Response,
        mockNext
      );

      expect(mockTaskService.updateTask).toHaveBeenCalledWith(1, {
        title: 'Updated Task',
        description: 'Updated description',
        status: TaskStatus.COMPLETED,
        dueDateTime: '2024-12-31T23:59:59Z',
      });
      expect(mockResponse.redirect).toHaveBeenCalledWith('/tasks');
    });

    it('should handle update errors', async () => {
      mockRequest.params = { id: '1' };
      mockRequest.body = { title: 'Updated', status: TaskStatus.TODO };

      const error = new Error('Update failed');
      mockTaskService.updateTask.mockRejectedValue(error);

      await taskController.update(
        mockRequest as Request,
        mockResponse as Response,
        mockNext
      );

      expect(mockNext).toHaveBeenCalledWith(error);
      expect(mockResponse.redirect).not.toHaveBeenCalled();
    });
  });

  describe('updateStatus', () => {
    it('should update task status and return JSON response', async () => {
      mockRequest.params = { id: '1' };
      mockRequest.body = { status: TaskStatus.COMPLETED };

      const updatedTask: Task = {
        id: 1,
        title: 'Task',
        status: TaskStatus.COMPLETED,
        createdAt: '2024-01-01T10:00:00Z',
        updatedAt: '2024-01-15T16:00:00Z',
      };

      mockTaskService.updateTaskStatus.mockResolvedValue(updatedTask);

      await taskController.updateStatus(
        mockRequest as Request,
        mockResponse as Response,
        mockNext
      );

      expect(mockTaskService.updateTaskStatus).toHaveBeenCalledWith(1, TaskStatus.COMPLETED);
      expect(mockResponse.json).toHaveBeenCalledWith({ success: true });
      expect(mockResponse.redirect).not.toHaveBeenCalled();
    });

    it('should handle invalid status', async () => {
      mockRequest.params = { id: '1' };
      mockRequest.body = { status: 'INVALID' };

      const error = new Error('Invalid status');
      mockTaskService.updateTaskStatus.mockRejectedValue(error);

      await taskController.updateStatus(
        mockRequest as Request,
        mockResponse as Response,
        mockNext
      );

      expect(mockNext).toHaveBeenCalledWith(error);
      expect(mockResponse.json).not.toHaveBeenCalled();
    });
  });

  describe('destroy', () => {
    it('should delete task and redirect', async () => {
      mockRequest.params = { id: '1' };

      mockTaskService.deleteTask.mockResolvedValue();

      await taskController.destroy(
        mockRequest as Request,
        mockResponse as Response,
        mockNext
      );

      expect(mockTaskService.deleteTask).toHaveBeenCalledWith(1);
      expect(mockResponse.redirect).toHaveBeenCalledWith('/tasks');
    });

    it('should handle deletion errors', async () => {
      mockRequest.params = { id: '1' };

      const error = new Error('Cannot delete task');
      mockTaskService.deleteTask.mockRejectedValue(error);

      await taskController.destroy(
        mockRequest as Request,
        mockResponse as Response,
        mockNext
      );

      expect(mockNext).toHaveBeenCalledWith(error);
      expect(mockResponse.redirect).not.toHaveBeenCalled();
    });

    it('should handle non-existent task deletion', async () => {
      mockRequest.params = { id: '999' };

      const error = { response: { status: 404 } };
      mockTaskService.deleteTask.mockRejectedValue(error);

      await taskController.destroy(
        mockRequest as Request,
        mockResponse as Response,
        mockNext
      );

      expect(mockTaskService.deleteTask).toHaveBeenCalledWith(999);
      expect(mockNext).toHaveBeenCalledWith(error);
    });
  });
});
