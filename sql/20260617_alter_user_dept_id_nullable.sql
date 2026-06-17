-- 用户表 dept_id 允许为空：采购员、仓管、财务、总经理等职能角色不绑定部门。
ALTER TABLE "user" ALTER COLUMN dept_id DROP NOT NULL;
