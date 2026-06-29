package com.example.localskill.repo.skill

import com.example.localskill.model.SkillModel
import com.example.localskill.utils.Resource

interface SkillRepository {
    fun addSkill(skill: SkillModel, callback: (Resource<Unit>) -> Unit)
    fun getSkillsByWorker(workerId: String, callback: (Resource<List<SkillModel>>) -> Unit)
    fun updateSkill(skill: SkillModel, callback: (Resource<Unit>) -> Unit)
    fun deleteSkill(skillId: String, workerId: String, callback: (Resource<Unit>) -> Unit)
}
