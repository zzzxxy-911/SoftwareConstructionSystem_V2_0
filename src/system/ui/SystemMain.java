package system.ui;

import system.entity.CampusResource;
import system.entity.User;
import system.exception.*;
import system.util.FileUtil;
import system.util.ResourceFileUtil;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

/**
 * 规范落实：分层架构、权限控制、持久化、统一异常捕获
 */
public class SystemMain {

    // 内存数据维护
    private final static List<User> userList = FileUtil.loadUsers();
    private final static List<CampusResource> resourceList = ResourceFileUtil.loadResources();
    private static User currentUser = null; // 当前登录用户

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean isRunning = true;

        while (isRunning) {
            try {
                if (currentUser == null) {
                    isRunning = displayGuestMenu(scanner); // 接收返回值
                } else if ("admin".equals(currentUser.getRole())) {
                    displayAdminMenu(scanner);
                } else if ("student".equals(currentUser.getRole())) {
                    displayStudentMenu(scanner);
                }
            } catch (BusinessException e) {
                System.out.println("【操作提示】" + e.getMessage());
            } catch (Exception e) {
                System.out.println("【系统错误】系统异常，请联系管理员。");
            }
        }
        scanner.close();
    }

    // ================= 菜单展示模块 =================

    /**
     * 显示游客(未登录)菜单
     */
    private static boolean displayGuestMenu(Scanner scanner) {
        System.out.println("\n=== 校园资源系统 ===");
        System.out.println("1. 登录  2. 注册  3. 退出");
        System.out.print("请输入：");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                executeLogin(scanner);
                return true;
            case "2":
                executeRegister(scanner);
                return true;
            case "3":
                System.exit(0);
                return false;
            default:
                System.out.println("无效选项！");
                return true;
        }
    }

    /**
     * 显示管理员菜单
     */
    private static void displayAdminMenu(Scanner scanner) {
        System.out.println("\n--- 管理员菜单 ---");
        System.out.println("1. 新增资源  2. 查询全部资源  3. 修改资源  4. 删除资源  5. 注销");
        System.out.print("请输入：");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                addResource(scanner);
                break;
            case "2":
                displayResourceList();
                break;
            case "3":
                updateResource(scanner);
                break;
            case "4":
                deleteResource(scanner);
                break;
            case "5":
                currentUser = null;
                System.out.println("已注销。");
                break;
            default:
                System.out.println("无效选项！");
        }
    }

    /**
     * 显示学生菜单
     */
    private static void displayStudentMenu(Scanner scanner) {
        System.out.println("\n--- 学生菜单 ---");
        System.out.println("1. 查询可预约资源  2. 测试越权异常  3. 注销");
        System.out.print("请输入：");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                displayAvailableResource();
                break;
            case "2":
                throw new PermissionDeniedException("学生无权执行此操作！");
            case "3":
                currentUser = null;
                System.out.println("已注销。");
                break;
            default:
                System.out.println("无效选项！");
        }
    }

    // ================= 资源业务逻辑模块 =================

    /**
     * 添加资源
     */
    private static void addResource(Scanner scanner) {
        System.out.print("输入资源名称：");
        String name = scanner.nextLine();
        if (name.trim().isEmpty()) {
            throw new ParamEmptyException("资源名称不能为空！");
        }

        System.out.print("输入类型：");
        String type = scanner.nextLine();
        System.out.print("输入位置：");
        String location = scanner.nextLine();
        System.out.print("输入描述：");
        String desc = scanner.nextLine();

        // 生成唯一ID
        String id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        CampusResource res = new CampusResource(id, name, type, "AVAILABLE", location, desc);

        resourceList.add(res);
        ResourceFileUtil.saveResources(resourceList);
        System.out.println("资源添加成功！ID：" + id);
    }

    /**
     * 显示所有资源列表
     */
    private static void displayResourceList() {
        if (resourceList.isEmpty()) {
            System.out.println("系统暂无资源。");
            return;
        }
        for (CampusResource r : resourceList) {
            System.out.println(r.toString());
        }
    }

    /**
     * 显示可预约资源
     */
    private static void displayAvailableResource() {
        boolean isFound = false;
        for (CampusResource r : resourceList) {
            System.out.println(r);
        }
        if (!isFound) {
            System.out.println("当前没有可预约的资源。");
        }
    }

    /**
     * 更新资源信息
     */
    private static void updateResource(Scanner scanner) {
        System.out.print("请输入要修改的资源ID：");
        String id = scanner.nextLine();

        CampusResource target = queryResourceById(id);
        if (target == null) {
            throw new ResourceNotFoundException("找不到该ID对应的资源！");
        }

        System.out.print("请输入新名称：");
        target.setName(scanner.nextLine());

        System.out.print("请输入新状态(AVAILABLE/OCCUPIED/DISABLED)：");
        String status = scanner.nextLine();

        if (!status.equals("AVAILABLE") && !status.equals("OCCUPIED") && !status.equals("DISABLED")) {
            throw new BusinessException("状态非法！只能输入规定状态。");
        }
        target.setStatus(status);

        ResourceFileUtil.saveResources(resourceList);
        System.out.println("资源更新成功！");
    }

    /**
     * 删除资源
     */
    private static void deleteResource(Scanner scanner) {
        System.out.print("请输入要删除的资源ID：");
        String id = scanner.nextLine();

        CampusResource target = queryResourceById(id);
        if (target == null) {
            throw new ResourceNotFoundException("找不到该ID对应的资源！");
        }

        resourceList.remove(target);
        ResourceFileUtil.saveResources(resourceList);
        System.out.println("资源删除成功！");
    }

    /**
     * 按ID查询资源
     */
    private static CampusResource queryResourceById(String id) {
        for (CampusResource r : resourceList) {
            if (r.getId().equals(id)) return r;
        }
        return null;
    }

    // ================= 用户认证模块 =================

    /**
     * 执行登录逻辑
     */
    private static void executeLogin(Scanner scanner) {
        System.out.print("请输入账号：");
        String username = scanner.nextLine();
        System.out.print("请输入密码：");
        String password = scanner.nextLine();

        User targetUser = null;
        for (User user : userList) {
            if (user.getUsername().equals(username)) {
                targetUser = user;
                break;
            }
        }

        if (targetUser == null) {
            throw new UserNotExistsException("用户不存在！");
        }
        if (!targetUser.getPassword().equals(password)) {
            throw new WrongPasswordException("密码错误！");
        }

        currentUser = targetUser;
        System.out.println("登录成功！欢迎，" + currentUser.getUsername());
    }

    /**
     * 执行注册逻辑
     */
    private static void executeRegister(Scanner scanner) {
        System.out.print("请输入新账号：");
        String username = scanner.nextLine();
        if (username.trim().isEmpty()) {
            throw new ParamEmptyException("账号不能为空！");
        }

        for (User user : userList) {
            if (user.getUsername().equals(username)) {
                throw new UserExistsException("账号已存在！");
            }
        }

        System.out.print("请输入新密码：");
        String password = scanner.nextLine();
        if (password.length() < 6) {
            throw new ParamEmptyException("密码不能少于6位！");
        }

        System.out.print("请输入角色(admin/student)：");
        String role = scanner.nextLine();

        User newUser = new User(username, password, role);
        userList.add(newUser);
        FileUtil.saveUser(newUser);
        System.out.println("注册成功！请登录。");
    }
}