# Plan: Refactor CharacterState to Pure Data Interface

## Goal
Transform CharacterState from a Signal-based interface to a pure data interface, while maintaining the current public API as much as possible. Eventually apply the same pattern to CharacterStaging, with SignalManager orchestrating all reactive behavior.

## Current Analysis

**CharacterState currently has:**
- Core selections: `Var[Option[T]]` for background, powerSource, archetype, personality, health
- Computed values: `Signal[List[...]]` for allQualities, allPowers, allAbilities, etc.
- Health calculations: `Signal[Option[Int]]` and `Signal[Int]`

**Usage patterns:**
- UI components access these as `character.allQualities.map(...)` in reactive contexts
- Computed values combine multiple Vars with complex logic (die changes, staging, etc.)

## Proposed Approach

### Phase 1: Split CharacterState Interface

**Create two complementary interfaces:**

1. **CharacterData** (pure data interface):
```scala
trait CharacterData:
  // Core selections - raw values
  def background: Option[Background]
  def powerSource: Option[PowerSource]
  def archetype: Option[Archetype]
  def personality: Option[Personality]
  def health: Option[Int]

  // Computed values - pure functions
  def allQualities: List[(Quality, Die)]
  def allPowers: List[(Power, Die)]
  def allStagedAbilities: List[ChosenAbility]
  def allChosenAbilities: List[ChosenAbility]
  def allPrinciples: List[Principle]
  def allAbilities: List[Ability[_]]
  def redZoneHealth: Option[Int]
  def powerQualityHealth: Int
```

2. **CharacterState** (reactive wrapper):
```scala
trait CharacterState:
  // Access to pure data
  val data: CharacterData

  // Reactive Vars for core selections (maintain API)
  val background: Var[Option[Background]]
  val powerSource: Var[Option[PowerSource]]
  val archetype: Var[Option[Archetype]]
  val personality: Var[Option[Personality]]
  val health: Var[Option[Int]]

  // Reactive Signals for computed values (maintain API)
  val allQualities: Signal[List[(Quality, Die)]]
  val allPowers: Signal[List[(Power, Die)]]
  // ... etc
```

### Phase 2: Implementation Strategy

**CharacterModel becomes:**
- Implements both CharacterData and CharacterState
- CharacterData methods compute pure values from current state
- CharacterState Signals map from data methods: `val allQualities: Signal[List[(Quality, Die)]] = Signal.combine(...).map(_ => data.allQualities)`

**Benefits:**
- **Pure computation logic**: All business logic moves to CharacterData methods
- **Testable**: Can test computation without reactive framework
- **Flexible**: Can access pure data or reactive signals as needed
- **Backward compatible**: Existing UI code continues to work unchanged

### Phase 3: Extract Computation Logic

**Create CharacterComputation class:**
```scala
class CharacterComputation(characterData: CharacterData, characterStaging: CharacterStaging):
  def computeAllQualities: List[(Quality, Die)] = // pure computation
  def computeAllPowers: List[(Power, Die)] = // pure computation
  // ... etc
```

**CharacterModel delegates:**
```scala
class CharacterModel extends CharacterData with CharacterState:
  private val computation = CharacterComputation(this, this)

  // CharacterData implementation - pure
  def allQualities: List[(Quality, Die)] = computation.computeAllQualities

  // CharacterState implementation - reactive
  val allQualities: Signal[List[(Quality, Die)]] = Signal.combine(...).map(_ => allQualities)
```

### Phase 4: Apply to CharacterStaging

**Similar split:**
- **CharacterStagingData**: Pure staging data access
- **CharacterStaging**: Reactive wrapper with Observers

### Phase 5: SignalManager Role

**SignalManager becomes orchestrator:**
- Manages all Signal creation and combination
- Contains reactive logic currently scattered across traits
- Provides factory methods for creating reactive wrappers around pure data

## Migration Benefits

1. **Separation of concerns**: Pure business logic vs reactive framework
2. **Easier testing**: Test computation without Signals/Vars
3. **Performance**: Can optimize pure computations separately
4. **Flexibility**: Can use data synchronously when needed
5. **Maintainability**: Clear distinction between data and presentation

## API Compatibility

**Minimal breaking changes:**
- Existing `character.allQualities` Signal access continues to work
- New `character.data.allQualities` pure access available
- CharacterValidator can use pure data interface for simpler testing
- CharacterExporter can use pure data interface

This approach provides a migration path to pure data while maintaining the reactive API for UI components.

## Implementation Status

- [x] CharacterModel refactored into 5 interfaces (CharacterState, CharacterStaging, CharacterExport, CharacterValidation, SignalManager)
- [x] CharacterExporter extracted and tested
- [x] CharacterValidator extracted and tested
- [x] MockCharacterState and MockCharacterStager created for testing
- [ ] **NEXT**: Implement Phase 1 - Create CharacterData interface
- [ ] **NEXT**: Modify CharacterState to include data property
- [ ] **NEXT**: Update CharacterModel to implement both interfaces
- [ ] **NEXT**: Extract computation logic to CharacterComputation class
- [ ] **NEXT**: Apply same pattern to CharacterStaging
- [ ] **NEXT**: Enhance SignalManager role