import { NextFunction, Request, Response } from 'express';

import { TaskService } from '@/main/services/taskService';
import { TaskStatus } from '@/main/types/task';

export class TaskController {
  private readonly taskService: TaskService;

  constructor() {
    this.taskService = new TaskService();
  }

  async index(_req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const tasks = await this.taskService.getAllTasks();
      res.render('tasks/index', {
        title: 'Tasks',
        tasks,
      });
    } catch (error) {
      next(error);
    }
  }

  async create(_req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      res.render('tasks/create', {
        title: 'Create task',
        statuses: Object.values(TaskStatus),
      });
    } catch (error) {
      next(error);
    }
  }

  async store(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const { title, description, status, dueDateTime } = req.body;

      console.log('Creating task with data:', { title, description, status, dueDateTime });

      await this.taskService.createTask({
        title,
        description: description || '',
        status,
        dueDateTime: dueDateTime || undefined,
      });

      res.redirect('/tasks');
    } catch (error) {
      console.error('Error in store method:', error);
      next(error);
    }
  }

  async edit(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const id = parseInt(req.params.id);
      const task = await this.taskService.getTaskById(id);

      res.render('tasks/edit', {
        title: 'Edit Task',
        task,
        statuses: Object.values(TaskStatus),
      });
    } catch (error) {
      next(error);
    }
  }

  async update(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const id = parseInt(req.params.id);
      const { title, description, status, dueDateTime } = req.body;

      await this.taskService.updateTask(id, {
        title,
        description: description || '',
        status,
        dueDateTime: dueDateTime || undefined,
      });

      res.redirect('/tasks');
    } catch (error) {
      next(error);
    }
  }

  async updateStatus(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const id = parseInt(req.params.id);
      const { status } = req.body;

      await this.taskService.updateTaskStatus(id, status);
      res.json({ success: true });
    } catch (error) {
      next(error);
    }
  }

  async destroy(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const id = parseInt(req.params.id);
      await this.taskService.deleteTask(id);
      res.redirect('/tasks');
    } catch (error) {
      next(error);
    }
  }
}
