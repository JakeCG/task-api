import axios, { AxiosInstance } from 'axios';

import { Task, TaskRequest, TaskStatus } from '@/main/types/task';

export class TaskService {
  private readonly api: AxiosInstance;

  constructor() {
    this.api = axios.create({
      baseURL: process.env.API_BASE_URL ?? 'http://backend:8080/api',
      timeout: 1000,
      headers: {
        'Content-Type': 'application/json',
      },
    });
  }

  async getAllTasks(): Promise<Task[]> {
    const response = await this.api.get<Task[]>('/tasks/get-all-tasks');
    return response.data;
  }

  async getTaskById(id: number): Promise<Task> {
    const response = await this.api.get<Task>(`/tasks/${id}/get-task`);
    return response.data;
  }

  async createTask(task: TaskRequest): Promise<Task> {
    const response = await this.api.post<Task>('/tasks/create-task', task);
    return response.data;
  }

  async updateTask(id: number, task: TaskRequest): Promise<Task> {
    const response = await this.api.put<Task>(`/tasks/${id}/update-task`, task);
    return response.data;
  }

  async updateTaskStatus(id: number, status: TaskStatus): Promise<Task> {
    const response = await this.api.put<Task>(`/tasks/${id}/status?status=${status}`);
    return response.data;
  }

  async deleteTask(id: number): Promise<void> {
    await this.api.delete(`/tasks/${id}/delete-task`);
  }
}
