package com.example.localskill.repo.skill

import com.example.localskill.model.SkillModel
import com.example.localskill.utils.Constants
import com.example.localskill.utils.Resource
import com.example.localskill.utils.readableMessage
import com.google.firebase.database.FirebaseDatabase

class SkillRepositoryImpl(
    database: FirebaseDatabase = FirebaseDatabase.getInstance()
) : SkillRepository {
    private val skillsRef = database.getReference(Constants.SKILLS)
    private val workerSkillsRef = database.getReference(Constants.WORKER_SKILLS)

    override fun addSkill(skill: SkillModel, callback: (Resource<Unit>) -> Unit) {
        callback(Resource.Loading)
        val skillId = skill.id.ifBlank { skillsRef.push().key.orEmpty() }
        if (skillId.isBlank()) {
            callback(Resource.Error("Unable to create skill id"))
            return
        }
        if (skill.workerId.isBlank()) {
            callback(Resource.Error("Unable to find worker account"))
            return
        }

        val now = System.currentTimeMillis()
        val savedSkill = skill.copy(
            id = skillId,
            createdAt = skill.createdAt.takeIf { it > 0L } ?: now,
            updatedAt = now
        )
        val updates = mapOf<String, Any>(
            "${Constants.SKILLS}/$skillId" to savedSkill,
            "${Constants.WORKER_SKILLS}/${skill.workerId}/$skillId" to savedSkill
        )
        skillsRef.root.updateChildren(updates)
            .addOnSuccessListener { callback(Resource.Success(Unit)) }
            .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
    }

    override fun getSkillsByWorker(workerId: String, callback: (Resource<List<SkillModel>>) -> Unit) {
        callback(Resource.Loading)
        workerSkillsRef.child(workerId).get()
            .addOnSuccessListener { snapshot ->
                val skills = snapshot.children.mapNotNull { it.getValue(SkillModel::class.java) }
                    .sortedByDescending { it.updatedAt.takeIf { updatedAt -> updatedAt > 0L } ?: it.createdAt }
                callback(Resource.Success(skills))
            }
            .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
    }

    override fun updateSkill(skill: SkillModel, callback: (Resource<Unit>) -> Unit) {
        callback(Resource.Loading)
        if (skill.id.isBlank() || skill.workerId.isBlank()) {
            callback(Resource.Error("Unable to update skill"))
            return
        }

        val updatedSkill = skill.copy(updatedAt = System.currentTimeMillis())
        val updates = mapOf<String, Any>(
            "${Constants.SKILLS}/${skill.id}" to updatedSkill,
            "${Constants.WORKER_SKILLS}/${skill.workerId}/${skill.id}" to updatedSkill
        )
        skillsRef.root.updateChildren(updates)
            .addOnSuccessListener { callback(Resource.Success(Unit)) }
            .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
    }

    override fun deleteSkill(skillId: String, workerId: String, callback: (Resource<Unit>) -> Unit) {
        callback(Resource.Loading)
        if (skillId.isBlank() || workerId.isBlank()) {
            callback(Resource.Error("Unable to delete skill"))
            return
        }

        val updates = mapOf<String, Any?>(
            "${Constants.SKILLS}/$skillId" to null,
            "${Constants.WORKER_SKILLS}/$workerId/$skillId" to null
        )
        skillsRef.root.updateChildren(updates)
            .addOnSuccessListener { callback(Resource.Success(Unit)) }
            .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
    }
}
