import { Router } from 'express';

import { TaskController } from '@/main/controllers/taskController';

const router: Router = Router();
const taskController = new TaskController();

router.get('/', taskController.index.bind(taskController));
router.get('/create', taskController.create.bind(taskController));
router.post('/create', taskController.store.bind(taskController));
router.get('/:id/edit', taskController.edit.bind(taskController));
router.post('/:id/edit', taskController.update.bind(taskController));
router.post('/:id/status', taskController.updateStatus.bind(taskController));
router.post('/:id/delete', taskController.destroy.bind(taskController));

export { router as taskRoutes };
