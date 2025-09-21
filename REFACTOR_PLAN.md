# CharacterModel Refactor Plan

## Interface Design

### 1. CharacterState (Getter Interface)
```scala
trait CharacterState {
  def background: Option[Background]
  def powerSource: Option[PowerSource]
  def archetype: Option[Archetype]
  def personality: Option[Personality]
  def health: Option[Int]

  def currentPowers: List[(Power, Die)]
  def currentQualities: List[(Quality, Die)]
  def currentAbilities: List[ChosenAbility]
  def currentPrinciples: List[Principle]

  def redZoneHealth: Option[Int]
  def calculatedHealth: Int
  def statusDice: Map[Status, Die]
}
```

### 2. CharacterStaging (Setter Interface with View Preservation)
```scala
trait CharacterStaging {
  def changeBackground(background: Background): Unit
  def changePowerSource(powerSource: PowerSource): Unit
  def changeArchetype(archetype: Archetype): Unit
  def changePersonality(personality: Personality): Unit
  def setHealth(health: Int): Unit

  def addPower(stagingKey: StagingKey, power: Power, die: Die): Unit
  def removePower(stagingKey: StagingKey, power: Power): Unit
  def addQuality(stagingKey: StagingKey, quality: Quality, die: Die): Unit
  def removeQuality(stagingKey: StagingKey, quality: Quality): Unit

  def addAbility(stagingKey: StagingKey, ability: Ability[_]): Unit
  def removeAbility(stagingKey: StagingKey, ability: Ability[_]): Unit
  def toggleAbility(stagingKey: StagingKey, ability: ChosenAbility): Unit

  def addAbilityChoice(stagingKey: StagingKey, ability: AbilityTemplate, choice: AbilityChoice): Unit
  def removeAbilityChoice(stagingKey: StagingKey, ability: AbilityTemplate, choice: AbilityChoice): Unit

  def upgradeDie(stagingKey: StagingKey, target: Quality | Power): Unit
  def downgradeDie(stagingKey: StagingKey, target: Quality | Power): Unit

  def getStagedPowers(stagingKey: StagingKey): List[(Power, Die)]
  def getStagedQualities(stagingKey: StagingKey): List[(Quality, Die)]
  def getStagedAbilities(stagingKey: StagingKey): List[Ability[_]]
  def getAbilityChoices(stagingKey: StagingKey): Map[AbilityKey, ChosenAbility]
}
```

### 3. CharacterExport (Export Interface)
```scala
trait CharacterExport {
  def exportCharacter(state: CharacterState): CharacterModelExport
  def renderCharacter(state: CharacterState): String
  def exportPowers(state: CharacterState): List[(Power, Die)]
  def exportQualities(state: CharacterState): List[(Quality, Die)]
  def exportAbilities(state: CharacterState): List[ChosenAbility]
  def exportPrinciples(state: CharacterState): List[Principle]
  def exportStatusDice(state: CharacterState): Map[Status, Die]
}
```

### 4. CharacterValidation (Validation Interface)
```scala
trait CharacterValidation {
  def isBackgroundValid(state: CharacterState, staging: CharacterStaging): Boolean
  def isPowerSourceValid(state: CharacterState, staging: CharacterStaging): Boolean
  def isArchetypeValid(state: CharacterState, staging: CharacterStaging): Boolean
  def isPersonalityValid(state: CharacterState, staging: CharacterStaging): Boolean
  def areRedAbilitiesValid(state: CharacterState, staging: CharacterStaging): Boolean
  def isHealthValid(state: CharacterState): Boolean
  def isCharacterComplete(state: CharacterState, staging: CharacterStaging): Boolean

  def validateBackground(state: CharacterState, staging: CharacterStaging): ValidationResult
  def validatePowerSource(state: CharacterState, staging: CharacterStaging): ValidationResult
  def validateArchetype(state: CharacterState, staging: CharacterStaging): ValidationResult
  def validatePersonality(state: CharacterState, staging: CharacterStaging): ValidationResult
  def validateRedAbilities(state: CharacterState, staging: CharacterStaging): ValidationResult
  def validateHealth(state: CharacterState): ValidationResult
  def validateCharacter(state: CharacterState, staging: CharacterStaging): List[ValidationResult]
}

case class ValidationResult(isValid: Boolean, errors: List[String] = List.empty)
```

### 5. SignalManager (Reactive Signal Management)
```scala
trait SignalManager {
  // Core state signals
  def backgroundSignal: Signal[Option[Background]]
  def powerSourceSignal: Signal[Option[PowerSource]]
  def archetypeSignal: Signal[Option[Archetype]]
  def personalitySignal: Signal[Option[Personality]]
  def healthSignal: Signal[Option[Int]]

  // Computed signals for current view
  def currentPowersSignal: Signal[List[(Power, Die)]]
  def currentQualitiesSignal: Signal[List[(Quality, Die)]]
  def currentAbilitiesSignal: Signal[List[ChosenAbility]]
  def currentPrinciplesSignal: Signal[List[Principle]]

  // Health signals
  def redZoneHealthSignal: Signal[Option[Int]]
  def calculatedHealthSignal: Signal[Int]

  // Validation signals
  def backgroundValidSignal: Signal[Boolean]
  def powerSourceValidSignal: Signal[Boolean]
  def archetypeValidSignal: Signal[Boolean]
  def personalityValidSignal: Signal[Boolean]
  def redAbilitiesValidSignal: Signal[Boolean]
  def healthValidSignal: Signal[Boolean]
  def characterCompleteSignal: Signal[Boolean]

  // Export signal
  def exportSignal: Signal[CharacterModelExport]

  // Observers for state changes
  def backgroundObserver: Observer[Background]
  def powerSourceObserver: Observer[PowerSource]
  def archetypeObserver: Observer[Archetype]
  def personalityObserver: Observer[Personality]
  def healthObserver: Observer[Int]

  // Staging-specific signals
  def powersSignal(stagingKey: Signal[Option[StagingKey]]): Signal[List[(Power, Die)]]
  def qualitiesSignal(stagingKey: Signal[Option[StagingKey]]): Signal[List[(Quality, Die)]]
  def abilitiesSignal(stagingKey: Signal[Option[StagingKey]]): Signal[List[Ability[_]]]
  def abilityChoicesSignal(stagingKey: StagingKey): Signal[Map[AbilityKey, ChosenAbility]]
  def abilitySelectedSignal(stagingKey: StagingKey, ability: Signal[Option[ChosenAbility]]): Signal[Boolean]

  // Staging observers
  def addPowerObserver(stagingKey: StagingKey): Observer[(Power, Die)]
  def removePowerObserver(stagingKey: StagingKey): Observer[(Power, Die)]
  def addQualityObserver(stagingKey: StagingKey): Observer[(Quality, Die)]
  def removeQualityObserver(stagingKey: StagingKey): Observer[(Quality, Die)]
  def addAbilityObserver(stagingKey: StagingKey): Observer[Ability[_]]
  def removeAbilityObserver(stagingKey: StagingKey): Observer[Ability[_]]
  def toggleAbilityObserver(stagingKey: StagingKey): Observer[ChosenAbility]
  def addAbilityChoiceObserver(stagingKey: StagingKey, ability: AbilityTemplate): Observer[AbilityChoice]
  def removeAbilityChoiceObserver(stagingKey: StagingKey, ability: AbilityTemplate): Observer[AbilityChoice]
  def upgradeObserver(stagingKey: StagingKey): Observer[Quality | Power]
  def downgradeObserver(stagingKey: StagingKey): Observer[Quality | Power]
}
```

## Method Mapping from Current Implementation

### CharacterExport (lines 553-577, CharacterModelExport class lines 14-65)
- `forExport` signal → `exportCharacter(state: CharacterState)`
- `CharacterModelExport.render` → `renderCharacter(state: CharacterState)`

### CharacterState (lines 68-88, computed signals 317-404)
- Current values from Vars + computed signals

### CharacterStaging (lines 69-71, 74-75, 78-79, 82-86, 108-121, 131-140, 150-183, 235-263, 288-303)
- All staging operations and observers

### CharacterValidation (lines 443-549)
- All validation signals

### SignalManager (all Var, Signal, Observer declarations)
- All reactive infrastructure