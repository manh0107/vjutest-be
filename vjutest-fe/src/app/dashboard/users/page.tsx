const handleUpdateUser = async (userData: Partial<User>, file?: File) => {
  if (!selectedUser?.id || !currentUser?.id) return
  try {
    const getRoleId = (role: string | undefined) => {
      switch(role) {
        case 'ROLE_ADMIN': return 1;
        case 'ROLE_TEACHER': return 2;
        case 'ROLE_USER': return 3;
        default: return 3;
      }
    };
    const { department, major, ...rest } = userData;
    const dataToSend = {
      ...rest,
      department: department?.id ? { id: department.id, name: department.name } : undefined,
      major: major?.id ? { id: major.id, name: major.name } : undefined,
      role: { id: getRoleId(typeof rest.role === 'string' ? rest.role : '') }
    };

    // Log data before sending
    console.log('Data to send:', JSON.stringify(dataToSend, null, 2));

    let updatedUser;
    if (file) {
      const formData = new FormData();
      formData.append('user', JSON.stringify(dataToSend));
      formData.append('file', file);
      console.log('FormData content:', {
        user: JSON.stringify(dataToSend),
        hasFile: !!file
      });
      updatedUser = await userService.updateUser(selectedUser.id, formData, currentUser.id, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
    } else {
      updatedUser = await userService.updateUser(selectedUser.id, dataToSend, currentUser.id);
    }

    // ... existing code ...
  } catch (error) {
    // ... existing code ...
  }
} 