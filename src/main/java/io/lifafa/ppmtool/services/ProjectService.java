package io.lifafa.ppmtool.services;

import io.lifafa.ppmtool.domain.Backlog;
import io.lifafa.ppmtool.domain.Project;
import io.lifafa.ppmtool.domain.User;
import io.lifafa.ppmtool.exceptions.ProjectIdException;
import io.lifafa.ppmtool.exceptions.ProjectNotFoundException;
import io.lifafa.ppmtool.repositories.BacklogRepository;
import io.lifafa.ppmtool.repositories.ProjectRepository;
import io.lifafa.ppmtool.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ProjectService {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private BacklogRepository backlogRepository;

    @Autowired
    private UserRepository userRepository;

    public Project saveOrUpdateProject(Project project, String username){

        if(project.getId() != null){
            Project existingProject = projectRepository.findByProjectIdentifier(project.getProjectIdentifier());
            if(existingProject !=null &&(!existingProject.getProjectLeader().equals(username))){
                throw new ProjectNotFoundException("Project not found in your account");
            }else if(existingProject == null){
                throw new ProjectNotFoundException("Project with ID: '"+project.getProjectIdentifier()+"' cannot be updated because it doesn't exist");
            }
        }


        try{

            User user = userRepository.findByUsername(username);

            project.setUser(user);
            project.setProjectLeader(user.getUsername());

            String id= project.getProjectIdentifier().toUpperCase();
            project.setProjectIdentifier(id);

            if(project.getId()==null){
                Backlog backlog = new Backlog();
                project.setBacklog(backlog);
                backlog.setProject(project);
                backlog.setProjectIdentifier(id);
            }

            if(project.getId()!=null){
                project.setBacklog(backlogRepository.findByProjectIdentifier(id));
            }

            return projectRepository.save(project);

        }catch(Exception e){
            throw new ProjectIdException("Project ID '"+project.getDescription().toUpperCase()+ "' already exist ");
        }

    }

    public Project findProjectByIdentifier(String projectId, String username){


        Project project = projectRepository.findByProjectIdentifier(projectId.toUpperCase());

        if(project == null){
            throw new ProjectIdException("Project ID '"+projectId+ "' does not exist ");

        }

        if(!project.getProjectLeader().equals(username)){
            throw  new ProjectNotFoundException("Project not found in your account");
        }
        return project;
    }

    public Iterable<Project> findAllProject(String username){
        return projectRepository.findAllByProjectLeader(username);
    }

    public void deleteProjectByIdentifier(String projectId,String username){


        projectRepository.delete(findProjectByIdentifier(projectId,username));
    }
}
